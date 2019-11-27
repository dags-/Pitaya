package me.dags.pitaya.cache;

import me.dags.pitaya.util.optional.Result;
import org.spongepowered.api.util.Identifiable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class Cache<T> {

    private final IdCache<T> cache;
    private final Function<UUID, T> func;

    public Cache(long time, TimeUnit unit, Supplier<T> supplier) {
        this(time, unit, uuid -> supplier.get());
    }

    public Cache(long time, TimeUnit unit, Function<UUID, T> func) {
        this.cache = new IdCache<>(time, unit);
        this.func = func;
    }

    public T must(UUID uuid) {
        return cache.compute(uuid, func);
    }

    public T must(Identifiable identifiable) {
        return must(identifiable.getUniqueId());
    }

    public <E> Result<T, E> drain(UUID uuid, E missing) {
        Optional<T> value = cache.get(uuid);
        if (value.isPresent()) {
            cache.remove(uuid);
            return Result.pass(value.get());
        }
        return Result.fail(missing);
    }

    public <E> Result<T, E> drain(Identifiable identifiable, E missing) {
        return drain(identifiable.getUniqueId(), missing);
    }
}
