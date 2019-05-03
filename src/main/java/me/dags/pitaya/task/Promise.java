package me.dags.pitaya.task;

import me.dags.pitaya.util.PluginUtils;
import org.spongepowered.api.scheduler.Task;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Promise<T> {

    private static final BiConsumer<Task, Throwable> defaultHandler = (t, e) -> e.printStackTrace();

    private final Object lock = new Object();
    private final Promise<?> root;
    private final Callable<T> callable;

    private Task task = null;
    private BiConsumer<Task, Throwable> handler = defaultHandler;

    private Promise(Callable<T> callable) {
        this.root = this;
        this.callable = callable;
    }

    private Promise(Promise<?> root, Callable<T> callable) {
        this.root = root;
        this.callable = callable;
    }

    private T call() throws Exception {
        return callable.call();
    }

    public void cancel() {
        synchronized (lock) {
            if (task != null) {
                task.cancel();
            }
        }
    }

    public Promise<T> handle(BiConsumer<Task, Throwable> handler) {
        synchronized (lock) {
            root.handler = handler;
        }
        return this;
    }

    public <V> Promise<V> then(Mapper<T, V> mapper) {
        return new Promise<>(root, () -> mapper.apply(call()));
    }

    public Promise<T> run(Consumer<T> consumer) {
        return run(Task.builder(), consumer);
    }

    public Promise<T> run(Task.Builder task, Consumer<T> consumer) {
        final Object plugin = PluginUtils.getCurrentPluginInstance();
        synchronized (lock) {
            this.task = task.async().execute(asyncTask(plugin, consumer)).submit(plugin);
        }
        return this;
    }

    private Runnable asyncTask(Object plugin, Consumer<T> consumer) {
        return () -> {
            try {
                final T result = call();
                Task.builder().execute(syncTask(result, consumer)).submit(plugin);
            } catch (Throwable t) {
                synchronized (lock) {
                    root.handler.accept(task, t);
                }
            }
        };
    }

    private Runnable syncTask(T t, Consumer<T> consumer) {
        return () -> consumer.accept(t);
    }

    public static <T> Promise<T> of(Callable<T> callable) {
        return new Promise<>(callable);
    }
}
