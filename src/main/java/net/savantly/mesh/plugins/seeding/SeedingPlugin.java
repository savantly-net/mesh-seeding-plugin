package net.savantly.mesh.plugins.seeding;

import org.pf4j.PluginWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gentics.mesh.plugin.AbstractPlugin;
import com.gentics.mesh.plugin.PluginConfigUtil;
import com.gentics.mesh.plugin.RestPlugin;
import com.gentics.mesh.plugin.env.PluginEnvironment;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.MaybeHelper;
import net.savantly.mesh.plugins.seeding.config.SeedingPluginConfig;
import net.savantly.mesh.plugins.seeding.process.MeshScriptProcessor;

public class SeedingPlugin extends AbstractPlugin implements RestPlugin {

	private static final Logger log = LoggerFactory.getLogger(SeedingPlugin.class);
	ObjectMapper mapper = new ObjectMapper();

	private SeedingPluginConfig config;

	public SeedingPlugin(PluginWrapper wrapper, PluginEnvironment env) {
		super(wrapper, env);
	}

	@Override
	public Completable initialize() {

		Maybe<String> maybe = MaybeHelper.toMaybe(handler -> {
			vertx().executeBlocking(fut -> {
				try {
					if (!getConfigFile().exists()) {
						config = writeConfig(new SeedingPluginConfig());
					} else {
						config = readConfig(SeedingPluginConfig.class);
					}
					log.info("Loaded config {\n" + PluginConfigUtil.getYAMLMapper().writeValueAsString(config) + "\n}");
					this.setConfig(config);
					
					MeshScriptProcessor.Builder()
						.withAdminClient(this.adminClient())
						.withConfig(getConfig())
						.withRxVertx(this.getRxVertx()).build().run();
				} catch (Exception e) {
					log.error(e);
				}
				fut.complete();
				
			}, handler);
		});
		
		return maybe.ignoreElement();
	}

	public SeedingPluginConfig getConfig() {
		return config;
	}

	public void setConfig(SeedingPluginConfig config) {
		this.config = config;
	}

}
