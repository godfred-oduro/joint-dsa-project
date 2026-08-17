package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import gh.ug.hospital.util.Parameters;

/**
 * Owner: 22160537
 *
 * A map: look something up by its key and get a value back.
 *
 * "Give me the location called L015" is a map question. So is "which
 * resource is handling request Q042". The whole program leans on this.
 *
 * Inside, it is a chained hash table. We keep the two classes separate
 * because the hash table is about how collisions are handled, while this is
 * about the everyday job of looking things up. The table size comes from
 * setting P1, which came from our index numbers, so menu option M8 can
 * change it during the defence.
 *
 * Named HashBackedMap because java.util already has Map.
 *
 * Speed: put, get and remove are all O(1) on average.
 */
public class HashBackedMap<K, V> {

    private final ChainedHashTable<K, V> table;

    /** Uses the project setting P1 for the table size. */
    public HashBackedMap() {
        this(Parameters.getHashTableSize());
    }

    public HashBackedMap(int slotCount) {
        this.table = new ChainedHashTable<>(slotCount);
    }

    public void put(K key, V value) {
        if (key == null) {
            throw new StructureException("A map key cannot be null.");
        }
        table.put(key, value);
    }

    /** Reads a value, or null if the key is not there. */
    public V get(K key) {
        return table.get(key);
    }

    /**
     * Reads a value, but complains instead of handing back null.
     * Use this when a missing key means the data is broken.
     */
    public V getOrFail(K key) {
        V value = table.get(key);
        if (value == null) {
            throw new StructureException("Nothing is stored under the key " + key);
        }
        return value;
    }

    public boolean containsKey(K key) {
        return table.containsKey(key);
    }

    public V remove(K key) {
        return table.remove(key);
    }

    public DynamicArray<K> keys() {
        return table.keys();
    }

    public DynamicArray<V> values() {
        return table.values();
    }

    public int size() {
        return table.size();
    }

    public boolean isEmpty() {
        return table.isEmpty();
    }

    public void clear() {
        table.clear();
    }

    public String toTraceString() {
        return "map with " + table.size() + " entries. " + table.toTraceString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("{");
        DynamicArray<K> allKeys = keys();
        for (int i = 0; i < allKeys.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            K key = allKeys.get(i);
            out.append(key).append("=").append(get(key));
        }
        out.append("}");
        return out.toString();
    }
}
