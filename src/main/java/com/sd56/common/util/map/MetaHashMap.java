package com.sd56.common.util.map;

import java.util.HashMap;

/**
 * This class is a HashMap that can store additional untyped metadata.
 * Can be used to store additional information about the data stored in the map, and can be used as a drop-in
 * replacement for HashMap.
 */
public class MetaHashMap<K, V> extends HashMap<K, V> implements MetaMap<K, V> {
    private final HashMap<K, Object> meta;

    public MetaHashMap() {
        super();
        this.meta = new HashMap<>();
    }

    /**
     * Checks if the key exists in the metadata map.
     * @param key The key to check.
     */
    public boolean containsKeyMeta(K key) {
        return this.meta.containsKey(key);
    }

    /**
     * Fetches the metadata value for the given key, or null if it doesn't exist.
     * @param key The key to fetch the metadata for.
     */
    public Object getMeta(K key) {
        return this.meta.get(key);
    }

    /**
     * Fetches the metadata value for the given key, or the default value if it doesn't exist.
     * @param key The key to fetch the metadata for.
     * @param defaultValue The default value to return if the key doesn't exist.
     */
    public Object getOrDefaultMeta(K key, Object defaultValue) {
        return this.meta.getOrDefault(key, defaultValue);
    }

    /**
     * Adds or updates the metadata for the given key.
     * @param key The key to add or update the metadata for.
     * @param metaValue The metadata value to add or update.
     */
    public void putMeta(K key, Object metaValue) {
        this.meta.put(key, metaValue);
    }

    /**
     * Adds the metadata for the given key if it doesn't already exist. Does nothing if the key already exists.
     * @param key The key to add the metadata for.
     * @param metaValue The metadata value to add.
     */
    public void putIfAbsentMeta(K key, Object metaValue) {
        this.meta.putIfAbsent(key, metaValue);
    }

    @Override
    public String toString() {
        return "MetaHashMap{"
                + "meta=" + meta + ", "
                + "data=" + super.toString()
                + '}';
    }
}
