package com.github.fallencrystal.lavender.api.pair;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public record Pair<A, B>(A first, B second) {
    public Optional<A> getFirstOptional() { return Optional.ofNullable(first); }
    public Optional<B> getSecondOptional() { return Optional.ofNullable(second); }

    public void apply(final @NotNull BiConsumer<A, B> consumer) {
        consumer.accept(first, second);
    }

    public <R> R let(final @NotNull BiFunction<A, B, R> function) {
        return function.apply(first, second);
    }

    public @NotNull Pair<A, B> copy() { return new Pair<>(first, second); }

    public @NotNull Supplier<A> getFirstSupplier() { return () -> first; }
    public @NotNull Supplier<B> getSecondSupplier() { return () -> second; }

    public static <A, B> Pair<A, B> of(final A first, final B second) {
        return new Pair<>(first, second);
    }

    public static <A, B> Pair<A, B> of(final @NotNull Map.Entry<A, B> entry) {
        return of(entry.getKey(), entry.getValue());
    }

    public static <A, B, T> @NotNull Set<Pair<A, B>> toPairSet(
            final @NotNull Collection<T> list,
            final @NotNull Function<T, A> firstProvider,
            final @NotNull Function<T, B> secondProvider) {
        final HashSet<Pair<A, B>> set = new HashSet<>();
        list.forEach(it -> set.add(Pair.of(firstProvider.apply(it), secondProvider.apply(it))));
        return set;
    }

    public static <K, V, M extends Map<K, V>> M toMap(
            final @NotNull Collection<Pair<K, V>> pairs,
            final @NotNull Supplier<M> mapFactory) {
        final M map = mapFactory.get();
        pairs.forEach(it -> map.put(it.first, it.second));
        return map;
    }

    public static <K, V> Map<K, V> toMap(final @NotNull Collection<Pair<K, V>> pairs) {
        return toMap(pairs, HashMap::new);
    }

    public static <K, V> @NotNull Set<Pair<K, V>> fromMap(final @NotNull Map<K, V> map) {
        final Set<Pair<K, V>> set = new HashSet<>();
        map.entrySet().forEach(it -> set.add(of(it)));
        return set;
    }

    public static <K, V> @NotNull Set<Pair<V, K>> fromMapReversed(final @NotNull Map<K, V> map) {
        final Set<Pair<V, K>> set = new HashSet<>();
        map.forEach((k, v) -> set.add(Pair.of(v, k)));
        return set;
    }
}
