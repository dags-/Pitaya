package me.dags.pitaya.util;

public interface OptionalValue {

    boolean isPresent();

    default boolean isAbsent() {
        return !isPresent();
    }
}
