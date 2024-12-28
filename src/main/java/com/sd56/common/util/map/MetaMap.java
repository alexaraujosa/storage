package com.sd56.common.util.map;

import java.util.Map;

/**
 * This interface is a Map that can store additional untyped metadata.
 * Can be used to store additional information about the data stored in the map, and can be used as a drop-in
 *  replacement for HashMap.
 */
public interface MetaMap<K, V> extends Map<K, V> {
    /**
     * Checks if the key exists in the metadata map.
     * @param key The key to check.
     */
    boolean containsKeyMeta(K key);

    /**
     * Fetches the metadata value for the given key, or null if it doesn't exist.
     * @param key The key to fetch the metadata for.
     */
    Object getMeta(K key);

    /**
     * Fetches the metadata value for the given key, or the default value if it doesn't exist.
     * @param key The key to fetch the metadata for.
     * @param defaultValue The default value to return if the key doesn't exist.
     */
    Object getOrDefaultMeta(K key, Object defaultValue);

    /**
     * Adds or updates the metadata for the given key.
     * @param key The key to add or update the metadata for.
     * @param metaValue The metadata value to add or update.
     */
    void putMeta(K key, Object metaValue);

    /**
     * Adds the metadata for the given key if it doesn't already exist. Does nothing if the key already exists.
     * @param key The key to add the metadata for.
     * @param metaValue The metadata value to add.
     */
    void putIfAbsentMeta(K key, Object metaValue);
}
