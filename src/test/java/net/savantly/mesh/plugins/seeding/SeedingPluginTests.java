package net.savantly.mesh.plugins.seeding;

import java.util.Optional;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.gentics.mesh.core.rest.node.NodeResponse;
import com.gentics.mesh.core.rest.plugin.PluginResponse;
import com.gentics.mesh.core.rest.project.ProjectResponse;
import com.gentics.mesh.core.rest.schema.MicroschemaListResponse;
import com.gentics.mesh.core.rest.schema.SchemaListResponse;
import com.gentics.mesh.core.rest.schema.impl.SchemaResponse;
import com.gentics.mesh.rest.client.MeshRequest;
import com.gentics.mesh.rest.client.MeshResponse;
import com.gentics.mesh.rest.client.MeshRestClient;
import com.gentics.mesh.test.local.MeshLocalServer;

public class SeedingPluginTests {


	private static final String apiName = "seeder";
	private static final String schemaName = "example_schema";
	private static final String microSchemaName = "example_microschema";
	private static final String projectName = "example";
	private static final String node1 = "b60a50dbe8354acd8a50dbe835bacd09";
	private static final String node2 = "a59ac0b6338f42c69ac0b6338f22c646";
	private static final String node3 = "73aeb52bc5344345aeb52bc5345345c1";

	@ClassRule
	public static final MeshLocalServer server = new MeshLocalServer().withInMemoryMode()
			.withPlugin(SeedingTestPlugin.class, apiName).waitForStartup();

	
	@Test
	public void testPluginLoad() throws InterruptedException {
		MeshRestClient client = server.client();
		MeshRequest<PluginResponse> pluginRequest = client.findPlugin(apiName);
		PluginResponse pluginResponse = pluginRequest.blockingGet();

		Assert.assertEquals("the plugin should be registered", apiName, pluginResponse.getId());
	}
	
	@Test
	public void testProjectCreated() throws InterruptedException {
		MeshRestClient client = server.client();
		ProjectResponse project = client.findProjectByName(projectName).blockingGet();

		Assert.assertEquals("the project should be created", projectName, project.getName());
	}

	@Test
	public void schemaAdded() throws InterruptedException {
		MeshRestClient client = server.client();
		SchemaListResponse schemas = client.findSchemas(projectName).blockingGet();
		Optional<SchemaResponse> result = schemas.getData().stream().filter(s -> s.getName().contentEquals(schemaName)).findFirst();
		Assert.assertTrue("the example schema should be created", result.isPresent());
	}
	
	@Test
	public void microSchemaAdded() {
		MeshRestClient client = server.client();
		MicroschemaListResponse schemas = client.findMicroschemas(projectName).blockingGet();
		boolean found = schemas.getData().stream().anyMatch(s -> s.getName().contentEquals(microSchemaName));
		Assert.assertTrue("the example microschema should be created", found);

	}
	
	@Test
	public void testNode1Added() {
		assertNodeExists(node1);
	}

	@Test
	public void testNode2Added() {
		assertNodeExists(node2);
	}

	@Test
	public void testNode3Added() {
		assertNodeExists(node3);
	}

	private void assertNodeExists(String uuid) {
		MeshRestClient client = server.client();
		MeshResponse<NodeResponse> response = client.findNodeByUuid(projectName, uuid).getResponse().blockingGet();
		Assert.assertEquals("node should be created", 200, response.getStatusCode());
	}
}
