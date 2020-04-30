package net.savantly.mesh.plugins.seeding.process;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.stringtemplate.v4.ST;

import com.gentics.mesh.core.rest.group.GroupCreateRequest;
import com.gentics.mesh.core.rest.group.GroupResponse;
import com.gentics.mesh.core.rest.microschema.impl.MicroschemaCreateRequest;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.project.ProjectCreateRequest;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.role.RoleCreateRequest;
import com.gentics.mesh.core.rest.role.RoleResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.json.JsonUtil;
import com.gentics.mesh.rest.client.MeshResponse;
import com.gentics.mesh.rest.client.MeshRestClient;
import com.gentics.mesh.rest.client.MeshRestClientMessageException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import net.savantly.mesh.plugins.seeding.config.MeshScript;
import net.savantly.mesh.plugins.seeding.config.SeedingPluginConfig;

public class MeshScriptProcessor {

	private final Logger log = LoggerFactory.getLogger(MeshScriptProcessor.class);
	
	private MeshRestClient adminClient;
	private Vertx vertx;
	private SeedingPluginConfig config;
	private Map<String, RoleResponse> createRoles = new HashMap<>();
	private Map<String, GroupResponse> createdGroups = new HashMap<>();

	private MeshScriptProcessor() {
	}

	public static MeshScriptProcessBuilder Builder() {
		return new MeshScriptProcessBuilder();
	}

	public void run() throws Exception {
		
		for (Entry<String, MeshScript> projectScripts : config.getMeshScripts().entrySet()) {
			
			final String scriptName = projectScripts.getKey();
			log.info("executing mesh script: {}", scriptName);
			final MeshScript mScript = projectScripts.getValue();
			final Map<String, Object> env = new HashMap<>();
			final String projectName = mScript.getProjectName();
			
			// add the project root node reference to the env vars, so the json files can reference it
			env.put("project", getProjectRootNode(projectName).getRootNode());
			
			
			for (Entry<String, String> entry : mScript.getMicroSchemaFiles().entrySet()) {
				if(fileIsReadable(entry.getValue())) {
					microSchemaCreationHandler(entry, projectName, mScript);
				} else {
					throw missingFile(entry);
				}
			}
			
			for (Entry<String, String> entry : mScript.getSchemaFiles().entrySet()) {
				if(fileIsReadable(entry.getValue())) {
					schemaCreationHandler(entry, projectName, mScript);
				} else {
					throw missingFile(entry);
				}
			}

			for (Entry<String, String> entry : mScript.getNodeFiles().entrySet()) {
				if(fileIsReadable(entry.getValue())) {
					nodeCreationHandler(entry, env, mScript);
				} else {
					throw missingFile(entry);
				}
			}
			
			for (String entry : mScript.getRoles()) {
				createRole(entry, projectName);
			}
			
			for (String entry : mScript.getGroups()) {
				createGroup(entry, projectName);
			}
			
			for (Entry<String, String> entry : mScript.getRolesToGroups().entrySet()) {
				addRoleToGroup(entry, projectName);
			}
		}
	}

	private void addRoleToGroup(Entry<String, String> entry, String projectName) {
		RoleResponse role = this.createRoles.get(entry.getKey());
		GroupResponse group = this.createdGroups.get(entry.getValue());
		this.adminClient.addRoleToGroup(group.getUuid(), role.getUuid()).blockingGet();
	}

	private void createGroup(String entry, String projectName) {
		Optional<GroupResponse> group = getGroup(entry);
		if (group.isPresent()) {
			this.createdGroups.put(entry, group.get());
		} else {
			this.createdGroups.put(entry, this.adminClient.createGroup(new GroupCreateRequest().setName(entry)).blockingGet());
		}
	}

	private Optional<GroupResponse> getGroup(String entry) {
		return this.adminClient.findGroups().blockingGet().getData().stream().filter(r -> r.getName().contentEquals(entry)).findFirst();
	}

	private void createRole(String entry, String projectName) {
		Optional<RoleResponse> role = getRole(entry);
		if (role.isPresent()) {
			this.createRoles.put(entry, role.get());
		} else {
			this.createRoles.put(entry, this.adminClient.createRole(new RoleCreateRequest().setName(entry)).blockingGet());
		}
	}

	private Optional<RoleResponse> getRole(String entry) {
		return this.adminClient.findRoles().blockingGet().getData().stream().filter(r -> r.getName().contentEquals(entry)).findFirst();
	}

	private void microSchemaCreationHandler(Entry<String, String> entry, String projectName, MeshScript mScript) {
		Buffer fileBuffer = vertx.fileSystem().readFileBlocking(entry.getValue());
		String schemaJson = fileBuffer.toString(StandardCharsets.UTF_8.name());

		// parse it into a json object
		MicroschemaCreateRequest schemaRequest = JsonUtil.readValue(schemaJson, MicroschemaCreateRequest.class);
		final String uuid = entry.getKey();

		// See if it already exists in the db
		MeshResponse<SchemaResponse> r = this.adminClient.findSchemaByUuid(uuid).getResponse().blockingGet();
		if (r.getStatusCode() == 200) {
			log.info("Schema already exists: {}", schemaRequest.getName());
		} else {
			log.info("creating microschema: {}", schemaRequest.getName());
			this.adminClient.createMicroschema(uuid, schemaRequest).getResponse().blockingGet();
			log.info("assigning microschema to project: {}", schemaRequest.getName());
			this.adminClient.assignMicroschemaToProject(projectName, uuid).getResponse().blockingGet();
		}
	}

	private void schemaCreationHandler(Entry<String, String> entry, String projectName, MeshScript mScript) {
		Buffer fileBuffer = vertx.fileSystem().readFileBlocking(entry.getValue());
		String schemaJson = fileBuffer.toString(StandardCharsets.UTF_8.name());

		// parse it into a json object
		SchemaCreateRequest schemaRequest = JsonUtil.readValue(schemaJson, SchemaCreateRequest.class);
		final String uuid = entry.getKey();

		// See if it already exists in the db
		MeshResponse<SchemaResponse> r = this.adminClient.findSchemaByUuid(uuid).getResponse().blockingGet();
		if (r.getStatusCode() == 200) {
			log.info("Schema already exists: {}", schemaRequest.getName());
		} else {
			log.info("creating schema: {}", schemaRequest.getName());
			this.adminClient.createSchema(uuid, schemaRequest).blockingGet();
			log.info("assigning schema to project: {}", schemaRequest.getName());
			this.adminClient.assignSchemaToProject(projectName, uuid).blockingGet();
		}
	}

	private void nodeCreationHandler(Entry<String, String> entry, Map<String, Object> env, MeshScript mScript) {
		Buffer fileBuffer = vertx.fileSystem().readFileBlocking(entry.getValue());
		String nodeString = fileBuffer.toString();
		String processedNode = replaceVariables(nodeString, env);
		NodeResponse result = createNode(mScript.getProjectName(), entry.getKey(), processedNode);
		env.put(entry.getKey(), result);
	}

	private Exception missingFile(Entry<String, String> entry) {
		// TODO Auto-generated method stub
		return new RuntimeException("invalid configuration. file is not found/readable: " + entry.getValue());
	}

	private boolean fileIsReadable(String path) {
		// Throw an exception if the file doesn't exist
		if (!vertx.fileSystem().existsBlocking(path)) {
			log.error("postman collection file doesn't exist: " + path);
			vertx.fileSystem().readDir(".", dir -> {
				log.info("current working directory contents");
				dir.result().forEach(s -> {
					log.info("{}", s);
				});
			});
			return false;
		} else {
			return true;
		}
	}

	private NodeResponse createNode(String project, String uuid, String processedNode) {
		log.info("creating node: " + processedNode);
		NodeCreateRequest request = JsonUtil.readValue(processedNode, NodeCreateRequest.class);
		NodeResponse response = this.adminClient.createNode(uuid, project, request).blockingGet();
		log.info("created node - uuid: " + response.getUuid() + ": " + response.getDisplayName());
		return response;
	}

	private String replaceVariables(String nodeString, Map<String, Object> env) {
		ST stringTemplate = new ST(nodeString);
		for (Entry<String, Object> varEntry : env.entrySet()) {
			stringTemplate.add(varEntry.getKey(), varEntry.getValue());
		}
		return stringTemplate.render();
	}

	private ProjectResponse getProjectRootNode(String project) {
		try {
			return this.adminClient.findProjectByName(project).blockingGet();
		} catch (Exception ex) {
			if (ex.getCause().getClass().isAssignableFrom(MeshRestClientMessageException.class) &&
					((MeshRestClientMessageException)ex.getCause()).getStatusCode() == 404) {
					return createProject(project);
			}
		}
		throw new RuntimeException("couldn't find or create project:" + project); 
	}



	private ProjectResponse createProject(String project) {
		ProjectCreateRequest createProject = new ProjectCreateRequest()
				.setName(project)
				.setSchemaRef("folder");
		return this.adminClient.createProject(createProject).blockingGet();
	}

	public MeshScriptProcessor init() {
		return this;
	}

	public static class MeshScriptProcessBuilder {

		private MeshScriptProcessor runner;

		public MeshScriptProcessBuilder() {
			this.runner = new MeshScriptProcessor();
		}

		public MeshScriptProcessBuilder withRxVertx(Vertx vertx) {
			runner.vertx = vertx;
			return this;
		}

		public MeshScriptProcessBuilder withAdminClient(MeshRestClient adminClient) {
			runner.adminClient = adminClient;
			return this;
		}

		public MeshScriptProcessBuilder withConfig(SeedingPluginConfig config) {
			runner.config = config;
			return this;
		}

		public MeshScriptProcessor build() {
			if (notSet(runner.vertx)) {
				handleMissingParam("vertx");
			} else if (notSet(runner.adminClient)) {
				handleMissingParam("adminClient");
			} else if (notSet(runner.config)) {
				handleMissingParam("config");
			}
			return runner.init();
		}

		private void handleMissingParam(String string) {
			throw new RuntimeException(string + " parameter shoud be set, but it's missing");
		}

		private boolean notSet(Object o) {
			return (null == o || "".equals(o));
		}
	}

}
