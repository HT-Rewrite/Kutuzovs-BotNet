package me.kutuzov.entry;

import java.io.Serializable;

public class SerializableEntry<K, V> implements Serializable {
    public final K key;
    public final V value;

    public SerializableEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }
}