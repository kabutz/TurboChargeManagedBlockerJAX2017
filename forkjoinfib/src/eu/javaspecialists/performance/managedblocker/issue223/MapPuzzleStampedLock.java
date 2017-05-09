package eu.javaspecialists.performance.managedblocker.issue223;

import java.util.*;
import java.util.concurrent.locks.*;

public class MapPuzzleStampedLock<K, V> {
    private final Map<K, V> map = new HashMap<K, V>();
    // note - REENTRANT
    private final StampedLock sl = new StampedLock();

    public V putIfAbsent(K key, V value) {
        long stamp = sl.readLock();
        try {
            V v;
            while ((v = map.get(key)) == null) {
                long writeStamp = sl.tryConvertToWriteLock(stamp);
                if (writeStamp != 0L) {
                    stamp = writeStamp;
                    V check = map.put(key, value);
                    assert check != null;
                    return null;
                } else {
                    sl.unlockRead(stamp);
                    stamp = sl.writeLock();
                }
            }
            return v;
        } finally {
            sl.unlock(stamp);
        }
    }

    public static void main(String... args) {
        System.out.println(new MapPuzzleStampedLock<Integer, String>()
            .putIfAbsent(42, "hello")); // null or "hello" or another String?
    }
}
