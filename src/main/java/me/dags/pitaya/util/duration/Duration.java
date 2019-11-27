package me.dags.pitaya.util.duration;

import me.dags.config.Node;
import org.spongepowered.api.data.persistence.DataTranslator;

import java.util.concurrent.TimeUnit;

public class Duration implements Node.Value<Duration> {

    public static final DataTranslator<Duration> TRANSLATOR = new DurationTranslator();

    public final long duration;
    public final TimeUnit unit;

    public Duration(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    public long getNano() {
        return unit.toNanos(duration);
    }

    public long getMS() {
        return unit.toMillis(duration);
    }

    public long getSecs() {
        return unit.toSeconds(duration);
    }

    public long getMins() {
        return unit.toMinutes(duration);
    }

    public long getHours() {
        return unit.toHours(duration);
    }

    public long getDays() {
        return unit.toDays(duration);
    }

    public static Duration mils(long time) {
        return new Duration(time, TimeUnit.MILLISECONDS);
    }

    public static Duration secs(long time) {
        return new Duration(time, TimeUnit.SECONDS);
    }

    public static Duration mins(long time) {
        return new Duration(time, TimeUnit.MINUTES);
    }

    public static Duration hrs(long time) {
        return new Duration(time, TimeUnit.HOURS);
    }

    @Override
    public Duration fromNode(Node node) {
        return new Duration(node.get("duration", 0L), node.get("unit", TimeUnit.class));
    }

    @Override
    public void toNode(Node node) {
        node.set("duration", duration);
        node.set("unit", unit);
    }
}