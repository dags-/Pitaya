package me.dags.pitaya.util.optional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Result<T, E> {

    private final T value;
    private final E error;

    private Result(T value, E error) {
        this.value = value;
        this.error = error;
    }

    public boolean isPass() {
        return value != null && error == null;
    }

    public boolean isFail() {
        return value == null && error != null;
    }

    public Result<T, E> onPass(Consumer<T> consumer) {
        if (isPass()) {
            consumer.accept(value);
        }
        return this;
    }

    public Result<T, E> onFail(Consumer<E> consumer) {
        if (isFail()) {
            consumer.accept(error);
        }
        return this;
    }

    public static <T, E> Result<T, E> pass(T value) {
        return new Result<>(value, null);
    }

    public static <T, E> Result<T, E> fail(E error) {
        return new Result<>(null, error);
    }

    public static <T, E> Result<T, E> of(Optional<T> optional, E error) {
        return optional.<Result<T, E>>map(Result::pass).orElseGet(() -> fail(error));
    }

    public static <T, E> Result<T, E> of(Optional<T> optional, Supplier<E> error) {
        return optional.<Result<T, E>>map(Result::pass).orElseGet(() -> fail(error.get()));
    }
}