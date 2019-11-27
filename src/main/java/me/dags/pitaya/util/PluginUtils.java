package me.dags.pitaya.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

public class PluginUtils {

    /**
     * Determine the currently active plugin and return it's PluginContainer instance
     *
     * @return the active plugin's PluginContainer
     */
    public static PluginContainer getCurrentPlugin() {
        Optional<PluginContainer> plugin = Sponge.getCauseStackManager().getCurrentCause().last(PluginContainer.class);
        if (!plugin.isPresent()) {
            plugin = Sponge.getCauseStackManager().getContext(EventContextKeys.PLUGIN);
        }
        return plugin.orElseThrow(() -> new IllegalStateException("Unable to determine active PluginContainer"));
    }

    /**
     * Determine the currently active plugin and return it's instance
     *
     * @return the active plugin's instance
     */
    public static Object getCurrentPluginInstance() {
        return getCurrentPlugin().getInstance().orElseThrow(() -> new IllegalStateException("Unable to get plugin instance"));
    }

    /**
     * Determine the currently active plugin and return it's instance
     *
     * @return the active plugin's instance
     */
    public static <T> T getCurrentPluginInstance(Class<T> type) {
        Object instance = getCurrentPluginInstance();
        if (!type.isInstance(instance)) {
            throw new IllegalStateException("Unable to get plugin instance");
        }
        return type.cast(instance);
    }
}
