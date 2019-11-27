package me.dags.pitaya.util.optional;

public interface OptionalValue {

    boolean isPresent();

    default boolean isAbsent() {
        return !isPresent();
    }
}
