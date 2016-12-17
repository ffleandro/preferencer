package com.github.preferencer.api.preference;

import android.content.SharedPreferences;

import java8.util.Objects;
import java8.util.function.Consumer;
import java8.util.function.Supplier;

/**
 * @author raunysouza
 */
abstract class BasePreference<T> {

    private SharedPreferences sharedPreferences;
    private String key;
    private T defaultValue;

    BasePreference(SharedPreferences sharedPreferences, String key, T defaultValue) {
        this.sharedPreferences = sharedPreferences;
        this.key = key;
        this.defaultValue = defaultValue;
    }

    protected abstract T internalGet(SharedPreferences preferences, String key, T defaultValue);

    protected abstract void internalPut(SharedPreferences.Editor editor, String key, T value);

    public T get() {
        return internalGet(sharedPreferences, key, defaultValue);
    }

    public void put(T value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        internalPut(editor, key, value);
        editor.apply();
    }

    public void remove() {
        sharedPreferences.edit().remove(key).apply();
    }

    public boolean exists() {
        return sharedPreferences.contains(key);
    }

    public T orElseGet(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return exists() ? get() : supplier.get();
    }

    public T orElse(T other) {
        return exists() ? get() : other;
    }

    public void ifExists(Consumer<T> consumer) {
        Objects.requireNonNull(consumer);
        if (exists()) {
            consumer.accept(get());
        }
    }

    public void putIfAbsent(T value) {
        if (!exists()) {
            put(value);
        }
    }

}
