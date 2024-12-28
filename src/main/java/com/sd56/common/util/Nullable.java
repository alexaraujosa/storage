package com.sd56.common.util;

public class Nullable<T> {
    private final T value;

    public Nullable(T value) {
        this.value = value;
    }

    public boolean isNull() {
        return this.value == null;
    }

    public T get() {
        return this.value;
    }

    public T getOrDefault(T defaultValue) {
        return this.value == null ? defaultValue : this.value;
    }

    public static <T> T getOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static <T> T getOrDefault(T value, T defaultValue, T nullValue) {
        return value == null ? defaultValue : value == nullValue ? defaultValue : value;
    }
}
