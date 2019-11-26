package me.dags.pitaya.util.cache;

import me.dags.pitaya.util.PluginUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.Identifiable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @Author <dags@dags.me>
 */
public class IdCache<T> extends TimedCache<UUID, T> {

    private static final Object lock = new Object();
    private static final AtomicBoolean registered = new AtomicBoolean(false);
    private static final List<WeakReference<IdCache>> caches = new LinkedList<>();

    public IdCache(long timeout, TimeUnit unit) {
        super(timeout, unit);
        register(this);
    }

    public IdCache(long timeout, TimeUnit unit, BiConsumer<UUID, T> listener) {
        super(timeout, unit, listener);
        register(this);
    }

    public boolean contains(Identifiable id) {
        return super.contains(id.getUniqueId());
    }

    public Optional<T> get(Identifiable id) {
        return super.get(id.getUniqueId());
    }

    public T compute(Identifiable id, Function<UUID, T> constructor) {
        return super.compute(id.getUniqueId(), constructor);
    }

    public void put(Identifiable id, T t) {
        super.put(id.getUniqueId(), t);
    }

    public void remove(Identifiable id) {
        super.remove(id.getUniqueId());
    }

    private static void register(IdCache cache) {
        synchronized (lock) {
            caches.add(new WeakReference<>(cache));

            if (!registered.getAndSet(true)) {
                Object plugin = PluginUtils.getCurrentPluginInstance();
                QuitListener listener = new QuitListener();
                Sponge.getEventManager().registerListener(plugin, ClientConnectionEvent.Disconnect.class, listener);
            }
        }
    }

    private static class QuitListener implements EventListener<ClientConnectionEvent.Disconnect> {
        @Override
        public void handle(ClientConnectionEvent.Disconnect event) throws Exception {
            synchronized (lock) {
                Iterator<WeakReference<IdCache>> iterator = caches.iterator();
                while (iterator.hasNext()) {
                    WeakReference<IdCache> reference = iterator.next();

                    if (reference.isEnqueued()) {
                        iterator.remove();
                        continue;
                    }

                    IdCache cache = reference.get();
                    if (cache == null) {
                        iterator.remove();
                        continue;
                    }

                    cache.remove(event.getTargetEntity());
                }
            }
        }
    }
}
