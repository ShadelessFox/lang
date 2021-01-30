package com.shade.lang.util;

import com.shade.lang.util.annotations.Nullable;

import java.util.Objects;

public class Pair<T1, T2> {
    private final T1 first;
    private final T2 second;

    public Pair(@Nullable T1 first, @Nullable T2 second) {
        this.first = first;
        this.second = second;
    }

    public T1 getFirst() {
        return first;
    }

    public T2 getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" + first + ", " + second + "}";
    }
}
