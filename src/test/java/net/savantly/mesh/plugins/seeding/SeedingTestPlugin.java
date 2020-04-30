package net.savantly.mesh.plugins.seeding;

import java.io.File;

import org.pf4j.PluginWrapper;

import com.gentics.mesh.plugin.env.PluginEnvironment;

import net.savantly.mesh.plugins.seeding.SeedingPlugin;

public class SeedingTestPlugin extends SeedingPlugin{

	public SeedingTestPlugin(PluginWrapper wrapper, PluginEnvironment env) {
		super(wrapper, env);
	}
	
	@Override
	protected File getConfigFile() {
		return new File("src/test/resources/plugin-test.yml");
	}

}
