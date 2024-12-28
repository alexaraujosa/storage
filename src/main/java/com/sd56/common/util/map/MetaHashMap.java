package com.sd56.common.util.map;

import java.util.HashMap;

public class MetaHashMap<K, V> extends HashMap<K, V> implements MetaMap<K, V> {
    private final HashMap<K, Object> meta;

    public MetaHashMap() {
        super();
        this.meta = new HashMap<>();
    }

    public boolean containsKeyMeta(K key) {
        return this.meta.containsKey(key);
    }

    public Object getMeta(K key) {
        return this.meta.get(key);
    }

    public Object getOrDefaultMeta(K key, Object defaultValue) {
        return this.meta.getOrDefault(key, defaultValue);
    }

    public void putMeta(K key, Object metaValue) {
        this.meta.put(key, metaValue);
    }

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
