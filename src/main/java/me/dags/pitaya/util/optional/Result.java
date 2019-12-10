package me.dags.pitaya.util.optional;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

    public <V> Result<V, E> map(Function<T, V> mapper) {
        if (isFail()) {
            return Result.fail(error);
        }
        return Result.pass(mapper.apply(value));
    }

    public <V> Result<V, E> flatMap(Function<T, Result<V, E>> mapper) {
        if (isFail()) {
            return Result.fail(error);
        }
        return mapper.apply(value);
    }

    public Result<T, E> filter(Predicate<T> predicate, E error) {
        if (isFail()) {
            return this;
        }
        if (predicate.test(value)) {
            return this;
        }
        return Result.fail(error);
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