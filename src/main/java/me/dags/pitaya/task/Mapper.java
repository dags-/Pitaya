package me.dags.pitaya.task;

@FunctionalInterface
public interface Mapper<T, V> {

    V apply(T t) throws Exception;
}
