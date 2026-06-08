package gasi.gps.platform.infrastructure.config;

import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Logs a startup summary for loaded PF4J plugins.
 *
 * @since 1.0.0
 */
@Configuration
public class PluginStartupLogger {

    private static final Logger LOG = LoggerFactory.getLogger(PluginStartupLogger.class);

    /**
     * Creates the plugin startup logger configuration.
     */
    public PluginStartupLogger() {
    }

    /**
     * Creates a startup logger.
     *
     * @param pluginManager PF4J plugin manager
     * @return command-line runner that logs plugin status
     */
    @Bean
    public CommandLineRunner logPlugins(PluginManager pluginManager) {
        return args -> {
            List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
            if (startedPlugins.isEmpty()) {
                LOG.info("No active plugins found. Ensure plugin JARs exist in the plugins directory.");
                return;
            }

            LOG.info("Loaded {} plugin(s).", startedPlugins.size());
            for (PluginWrapper plugin : startedPlugins) {
                LOG.info("Plugin id={}, version={}, class={}, state={}",
                        plugin.getPluginId(),
                        plugin.getDescriptor().getVersion(),
                        plugin.getDescriptor().getPluginClass(),
                        plugin.getPluginState());
            }
        };
    }
}
