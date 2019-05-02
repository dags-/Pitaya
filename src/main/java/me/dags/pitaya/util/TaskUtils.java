package me.dags.pitaya.util;

import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class TaskUtils {

    public static <T> void execute(Callable<T> async, Consumer<T> sync) {
        execute(async, sync, Exception::printStackTrace);
    }

    public static <T> void execute(Callable<T> async, Consumer<T> sync, Consumer<Exception> error) {
        try {
            final Object plugin = PluginUtils.getCurrentPluginInstance();
            Task.builder().async().execute(() -> {
                try {
                    final T t = async.call();
                    Task.builder().execute(() -> sync.accept(t)).submit(plugin);
                } catch (Exception e) {
                    Task.builder().execute(() -> error.accept(e)).submit(plugin);
                }
            }).submit(plugin);
        } catch (IllegalStateException e) {
            error.accept(e);
        }
    }
}
