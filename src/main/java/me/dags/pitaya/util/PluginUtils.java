package me.dags.pitaya.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

public class PluginUtils {

    public static PluginContainer getCurrentPlugin() {
        Optional<PluginContainer> plugin = Sponge.getCauseStackManager().getCurrentCause().last(PluginContainer.class);
        if (!plugin.isPresent()) {
            plugin = Sponge.getCauseStackManager().getContext(EventContextKeys.PLUGIN);
        }
        return plugin.orElseThrow(() -> new IllegalStateException("Unable to determine active PluginContainer"));
    }

    public static Object getCurrentPluginInstance() {
        return getCurrentPlugin().getInstance().orElseThrow(() -> new IllegalStateException("Unable to get plugin instance"));
    }
}
