package me.dags.pitaya.util.cache;

import me.dags.pitaya.util.PluginUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.Identifiable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Author <dags@dags.me>
 */
public class IdCache<T> extends TimedCache<UUID, T> {
    public IdCache(long timeout, TimeUnit unit) {
        super(timeout, unit);
        Sponge.getEventManager().registerListeners(PluginUtils.getCurrentPluginInstance(), this);
    }

    public IdCache(long timeout, TimeUnit unit, Supplier<Map<UUID, Value>> supplier) {
        super(timeout, unit, supplier);
        Sponge.getEventManager().registerListeners(PluginUtils.getCurrentPluginInstance(), this);
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

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        super.remove(event.getTargetEntity().getUniqueId());
    }
}
