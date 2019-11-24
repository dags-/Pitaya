package me.dags.pitaya.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @Author <dags@dags.me>
 */
public class TimedCache<K, V> {

    private static final long interval = 1000L;

    private final long timeout;
    private final TimeUnit unit;
    private final Map<K, Value> values;

    private long nextUpdate  = 0L;

    public TimedCache(long timeout, TimeUnit unit) {
        this(timeout, unit, HashMap::new);
    }

    public TimedCache(long timeout, TimeUnit unit, Supplier<Map<K, Value>> supplier) {
        this.values = supplier.get();
        this.timeout = timeout;
        this.unit = unit;
    }

    public boolean contains(K key) {
        update();
        return values.containsKey(key);
    }

    public Optional<V> get(K key) {
        update();
        Value value = values.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.refresh().value);
    }

    public V compute(K key, Function<K, V> constructor) {
        update();
        Value value = values.get(key);
        if (value == null) {
            value = new Value(constructor.apply(key));
            values.put(key, value);
            return value.value;
        }
        return value.refresh().value;
    }

    public void put(K key, V value) {
        update();
        values.put(key, new Value(value));
    }

    public void remove(K key) {
        update();
        values.remove(key);
    }

    private void update() {
        long now = System.currentTimeMillis();
        if (nextUpdate < now) {
            values.entrySet().removeIf(entry -> hasExpired(entry.getValue(), now));
            nextUpdate = now + interval;
        }
    }

    private boolean hasExpired(Value value, long now) {
        return value.timestamp < now;
    }

    public class Value {

        private final V value;
        private long timestamp = 0L;

        private Value(V value) {
            this.value = value;
            refresh();
        }

        private Value refresh() {
            this.timestamp = System.currentTimeMillis() + unit.toMillis(timeout);
            return this;
        }
    }
}
