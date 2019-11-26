package me.dags.pitaya.util.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @Author <dags@dags.me>
 */
public class TimedCache<K, V> {

    private static final long interval = 1000L;

    private final long timeout;
    private final TimeUnit unit;
    private final Map<K, Value> values;
    private final BiConsumer<K, V> listener;

    private long nextUpdate  = 0L;

    public TimedCache(long timeout, TimeUnit unit) {
        this(timeout, unit, (k, v) -> {});
    }

    public TimedCache(long timeout, TimeUnit unit, BiConsumer<K, V> listener) {
        this.values = new HashMap<>();
        this.listener = listener;
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

    @Override
    public boolean equals(Object o) {
        return o instanceof TimedCache && ((TimedCache) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return "Cache{"
                + "timeout=" + timeout
                + ",values=" + values.toString()
                + "}";
    }

    private void update() {
        long now = System.currentTimeMillis();
        if (nextUpdate < now) {
            Iterator<Map.Entry<K, Value>> iterator = values.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, Value> entry = iterator.next();
                if (hasExpired(entry.getValue(), now)) {
                    iterator.remove();
                    listener.accept(entry.getKey(), entry.getValue().value);
                }
            }
            nextUpdate = now + interval;
        }
    }

    private boolean hasExpired(Value value, long now) {
        return value.timestamp < now;
    }

    private class Value {

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
