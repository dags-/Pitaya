package me.dags.pitaya.util.optional;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class BiOptional<A, B> implements OptionalValue {

    private static final BiOptional empty = new BiOptional<>(Optional.empty(), Optional.empty());

    private final Optional<A> a;
    private final Optional<B> b;

    private BiOptional(Optional<A> a, Optional<B> b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean isPresent() {
        return a.isPresent() && b.isPresent();
    }

    public <T> Optional<T> map(BiFunction<A, B, T> function) {
        if (a.isPresent() && b.isPresent()) {
            return Optional.of(function.apply(a.get(), b.get()));
        }
        return Optional.empty();
    }

    public <T> Optional<T> flatMap(BiFunction<A, B, Optional<T>> function) {
        if (a.isPresent() && b.isPresent()) {
            return function.apply(a.get(), b.get());
        }
        return Optional.empty();
    }

    public BiOptional<A, B> filter(BiPredicate<A, B> filter) {
        if (a.isPresent() && b.isPresent()) {
            if (filter.test(a.get(), b.get())) {
                return this;
            }
        }
        return BiOptional.empty();
    }

    public BiOptional<A, B> ifPresent(BiConsumer<A, B> consumer) {
        if (a.isPresent() && b.isPresent()) {
            consumer.accept(a.get(), b.get());
        }
        return this;
    }

    public BiOptional<A, B> ifAbsent(Runnable consumer) {
        if (isAbsent()) {
            consumer.run();
        }
        return this;
    }

    public static <A, B> BiOptional<A, B> ofNullable(A a, B b) {
        return of(Optional.ofNullable(a), Optional.ofNullable(b));
    }

    public static <A, B> BiOptional<A, B> of(Optional<A> a, Optional<B> b) {
        return new BiOptional<>(a, b);
    }

    @SuppressWarnings("unchecked")
    public static <A, B> BiOptional<A, B> empty() {
        return empty;
    }
}
