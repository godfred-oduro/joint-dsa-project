package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22370298
 *
 * A hash table that handles clashes by keeping a small list in each slot.
 *
 * The idea: turn the key into a number, use that number to pick a slot in an
 * array, and store the item there. Looking it up does the same sum and goes
 * straight to the slot, so it takes about the same time whether there are
 * ten items or ten thousand.
 *
 * The problem: two different keys can land on the same slot. That is a
 * collision. This version deals with it by hanging a little chain off each
 * slot and walking the chain. Experiment E3 measures how the number of
 * collisions grows as the table fills up.
 *
 * In this project the keys are hospital card numbers and location codes,
 * both of which are unique text.
 *
 * Speed:
 *   put, get, remove   O(1) on average, O(n) if everything collides
 */
public class ChainedHashTable<K, V> {

    /** One item in a chain. */
    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private final Object[] slots;
    private int count;

    /** How many times a new key landed on a slot that was already in use. */
    private long collisions;

    public ChainedHashTable(int slotCount) {
        if (slotCount < 1) {
            slotCount = 1;
        }
        this.slots = new Object[slotCount];
        this.count = 0;
        this.collisions = 0;
    }

    /**
     * Turns a key into a slot number.
     *
     * Java's hashCode can be negative, and a negative array index would
     * crash, so we strip the sign bit with & 0x7fffffff.
     */
    private int slotFor(K key) {
        if (key == null) {
            throw new StructureException("A hash table key cannot be null.");
        }
        int raw = key.hashCode();
        return (raw & 0x7fffffff) % slots.length;
    }

    /** Stores a value. If the key is already there, the old value is replaced. */
    @SuppressWarnings("unchecked")
    public void put(K key, V value) {
        int slot = slotFor(key);
        Entry<K, V> chain = (Entry<K, V>) slots[slot];

        // Is the key already in this chain?
        Entry<K, V> walker = chain;
        while (walker != null) {
            if (walker.key.equals(key)) {
                walker.value = value;
                return;
            }
            walker = walker.next;
        }

        // Not there, so add a new entry at the front of the chain.
        if (chain != null) {
            collisions++;
        }
        Entry<K, V> fresh = new Entry<>(key, value);
        fresh.next = chain;
        slots[slot] = fresh;
        count++;
    }

    /** Reads a value, or null if the key is not there. */
    @SuppressWarnings("unchecked")
    public V get(K key) {
        Entry<K, V> walker = (Entry<K, V>) slots[slotFor(key)];
        while (walker != null) {
            if (walker.key.equals(key)) {
                return walker.value;
            }
            walker = walker.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /** Removes a key and hands back the value it held. */
    @SuppressWarnings("unchecked")
    public V remove(K key) {
        int slot = slotFor(key);
        Entry<K, V> walker = (Entry<K, V>) slots[slot];
        Entry<K, V> behind = null;

        while (walker != null) {
            if (walker.key.equals(key)) {
                if (behind == null) {
                    slots[slot] = walker.next;
                } else {
                    behind.next = walker.next;
                }
                count--;
                return walker.value;
            }
            behind = walker;
            walker = walker.next;
        }
        throw new StructureException(key + " is not in the hash table.");
    }

    /** Every key currently stored. */
    @SuppressWarnings("unchecked")
    public DynamicArray<K> keys() {
        DynamicArray<K> out = new DynamicArray<>();
        for (int i = 0; i < slots.length; i++) {
            Entry<K, V> walker = (Entry<K, V>) slots[i];
            while (walker != null) {
                out.add(walker.key);
                walker = walker.next;
            }
        }
        return out;
    }

    /** Every value currently stored. */
    @SuppressWarnings("unchecked")
    public DynamicArray<V> values() {
        DynamicArray<V> out = new DynamicArray<>();
        for (int i = 0; i < slots.length; i++) {
            Entry<K, V> walker = (Entry<K, V>) slots[i];
            while (walker != null) {
                out.add(walker.value);
                walker = walker.next;
            }
        }
        return out;
    }

    /**
     * How full the table is: items divided by slots.
     *
     * With chaining this can go above 1.0, because a slot can hold many
     * items. Experiment E3 plots collisions against this number.
     */
    public double loadFactor() {
        return (double) count / (double) slots.length;
    }

    public long getCollisions() {
        return collisions;
    }

    /** The longest chain in the table. A long chain means slow lookups. */
    @SuppressWarnings("unchecked")
    public int longestChain() {
        int longest = 0;
        for (int i = 0; i < slots.length; i++) {
            int length = 0;
            Entry<K, V> walker = (Entry<K, V>) slots[i];
            while (walker != null) {
                length++;
                walker = walker.next;
            }
            if (length > longest) {
                longest = length;
            }
        }
        return longest;
    }

    /** The average chain length across the slots that are actually used. */
    @SuppressWarnings("unchecked")
    public double averageChainLength() {
        int usedSlots = 0;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                usedSlots++;
            }
        }
        if (usedSlots == 0) {
            return 0.0;
        }
        return (double) count / (double) usedSlots;
    }

    public int size() {
        return count;
    }

    public int slotCount() {
        return slots.length;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
        count = 0;
        collisions = 0;
    }

    public String toTraceString() {
        return "slots=" + slots.length
                + "  items=" + count
                + "  load factor=" + String.format("%.3f", loadFactor())
                + "  collisions=" + collisions
                + "  longest chain=" + longestChain()
                + "  average chain=" + String.format("%.2f", averageChainLength());
    }
}
