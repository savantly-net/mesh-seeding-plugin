package net.savantly.mesh.plugins.seeding.config;

import java.util.HashMap;
import java.util.Map;

public class SeedingPluginConfig {
	
	private Map<String, MeshScript> meshScripts = new HashMap<>();
	
	private boolean haltOnError = true;


	/**
	 * @return Collection of scripts to run
	 * Key = arbitrary name for the mesh script
	 * Value = script properties
	 */
	public Map<String, MeshScript> getMeshScripts() {
		return meshScripts;
	}

	public void setMeshScripts(Map<String, MeshScript> meshScripts) {
		this.meshScripts = meshScripts;
	}
	

	/**
	 * @return Whether the execution of seed process halts if there is an error
	 */
	public boolean isHaltOnError() {
		return haltOnError;
	}

	public void setHaltOnError(boolean haltOnError) {
		this.haltOnError = haltOnError;
	}

}
