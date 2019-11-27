package me.dags.pitaya.text;

import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Format;
import me.dags.pitaya.util.PluginUtils;

import java.util.function.Supplier;

public class Formats {

    public static void init(Supplier<Format> format) {
        String plugin = PluginUtils.getCurrentPlugin().getId();
        init(plugin, format);
    }

    public static void init(String plugin, Supplier<Format> format) {
        Format current = Fmt.get(plugin);
        Format global = Fmt.copy();
        if (current.equals(global)) {
            Fmt.init(plugin, format.get());
        }
    }
}
