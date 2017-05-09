package eu.javaspecialists.performance.managedblocker.issue223;

import java.util.*;
import java.util.concurrent.locks.*;

public class MapPuzzle<K, V> {
    private final Map<K, V> map = new HashMap<>();
    // note - REENTRANT
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

    public V putIfAbsent(K key, V value) {
        rwlock.readLock().lock();
        try {
            V v = map.get(key);
            if (v != null) return v;
            rwlock.writeLock().lock();
            try {
                map.put(key, value);
                return null;
            } finally {
                rwlock.writeLock().unlock();
            }
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public static void main(String... args) {
        // null or "hello" or another String?
        System.out.println(new MapPuzzle<>().putIfAbsent(42, "hello"));
    }
}
