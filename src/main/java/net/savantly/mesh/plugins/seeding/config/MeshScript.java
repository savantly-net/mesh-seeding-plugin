package net.savantly.mesh.plugins.seeding.config;

import java.util.HashMap;
import java.util.Map;

public class MeshScript {
	
	private String projectName;
	private String schema = "folder";
	private Map<String, String> microSchemaFiles = new HashMap<>();
	private Map<String, String> schemaFiles = new HashMap<>();
	private Map<String, String> nodeFiles = new HashMap<>();
	private boolean publishNodes;
	private String[] roles;
	private String[] groups;
	private Map<String, String> rolesToGroups = new HashMap<>();

	/**
	 * @return the mesh project name [case sensitive]
	 */
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * 
	 * After adding the node, the newly created node reference will be added as an environment variable so it can be referenced in downstream node creations.
	 * The key in the map becomes the variable name.
	 * 
	 * @return A map of files that represent the node in json format.
	 */
	public Map<String, String> getNodeFiles() {
		return nodeFiles;
	}

	public void setPublishNodes(boolean publishNodes) {
		this.publishNodes = publishNodes;
	}

	public boolean getPublishNodes() {
		return publishNodes;
	}

	public void setNodeFiles(Map<String, String> nodeFiles) {
		this.nodeFiles = nodeFiles;
	}

	/**
	 * 
	 * Default value is "folder"
	 * 
	 * @return The schema name to use if creating the project
	 */
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * @return List of micro-schema file paths to create in sequential order
	 * These are created before the schemaFiles
	 */
	public Map<String, String> getMicroSchemaFiles() {
		return microSchemaFiles;
	}

	public void setMicroSchemaFiles(Map<String, String> microSchemas) {
		this.microSchemaFiles = microSchemas;
	}

	/**
	 * @return List of schemaFiles file paths to create in sequential order
	 */
	public Map<String, String> getSchemaFiles() {
		return schemaFiles;
	}

	public void setSchemaFiles(Map<String, String> schemas) {
		this.schemaFiles = schemas;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	public String[] getGroups() {
		return groups;
	}

	public void setGroups(String[] groups) {
		this.groups = groups;
	}

	public Map<String, String> getRolesToGroups() {
		return rolesToGroups;
	}

	public void setRolesToGroups(Map<String, String> rolesToGroups) {
		this.rolesToGroups = rolesToGroups;
	}


}
