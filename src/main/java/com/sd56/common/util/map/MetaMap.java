package com.sd56.common.util.map;

import java.util.Map;

public interface MetaMap<K, V> extends Map<K, V> {
    boolean containsKeyMeta(K key);

    Object getMeta(K key);

    Object getOrDefaultMeta(K key, Object defaultValue);

    void putMeta(K key, Object metaValue);

    void putIfAbsentMeta(K key, Object metaValue);
}
