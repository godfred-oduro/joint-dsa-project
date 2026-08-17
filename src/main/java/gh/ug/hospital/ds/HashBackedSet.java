package gh.ug.hospital.ds;

import gh.ug.hospital.util.Parameters;

/**
 * Owner: 22399010
 *
 * A set: a bag of things where nothing appears twice.
 *
 * Adding something that is already in the set does nothing. That is the
 * whole point. BFS and DFS use it to remember which locations they have
 * already visited, so they do not walk in circles.
 *
 * It is a thin layer over the map. We store the item as the key and ignore
 * the value, because a set is really a map where only the keys matter.
 *
 * The three joining operations are the interesting part:
 *   union         everything in either set
 *   intersection  only things in both
 *   difference    things in this set but not the other
 *
 * Named HashBackedSet because java.util already has Set.
 *
 * Speed: add, remove and contains are all O(1) on average.
 */
public class HashBackedSet<T> {

    /** A stand-in value. We only ever care about the keys. */
    private static final Boolean PRESENT = Boolean.TRUE;

    private final HashBackedMap<T, Boolean> items;

    public HashBackedSet() {
        this(Parameters.getHashTableSize());
    }

    public HashBackedSet(int slotCount) {
        this.items = new HashBackedMap<>(slotCount);
    }

    /** Adds an item. Hands back true only if it was not already there. */
    public boolean add(T item) {
        if (items.containsKey(item)) {
            return false;
        }
        items.put(item, PRESENT);
        return true;
    }

    /** Removes an item. Hands back true if it was there to remove. */
    public boolean remove(T item) {
        if (!items.containsKey(item)) {
            return false;
        }
        items.remove(item);
        return true;
    }

    public boolean contains(T item) {
        return items.containsKey(item);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    /** Everything in the set, as a list. */
    public DynamicArray<T> toArray() {
        return items.keys();
    }

    /** Everything in either set. */
    public HashBackedSet<T> union(HashBackedSet<T> other) {
        HashBackedSet<T> result = new HashBackedSet<>();

        DynamicArray<T> mine = toArray();
        for (int i = 0; i < mine.size(); i++) {
            result.add(mine.get(i));
        }

        DynamicArray<T> theirs = other.toArray();
        for (int i = 0; i < theirs.size(); i++) {
            result.add(theirs.get(i));
        }
        return result;
    }

    /** Only the things that are in both sets. */
    public HashBackedSet<T> intersection(HashBackedSet<T> other) {
        HashBackedSet<T> result = new HashBackedSet<>();
        DynamicArray<T> mine = toArray();
        for (int i = 0; i < mine.size(); i++) {
            T item = mine.get(i);
            if (other.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Things in this set that are not in the other one. */
    public HashBackedSet<T> difference(HashBackedSet<T> other) {
        HashBackedSet<T> result = new HashBackedSet<>();
        DynamicArray<T> mine = toArray();
        for (int i = 0; i < mine.size(); i++) {
            T item = mine.get(i);
            if (!other.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("{");
        DynamicArray<T> all = toArray();
        for (int i = 0; i < all.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(all.get(i));
        }
        out.append("}");
        return out.toString();
    }

    public String toTraceString() {
        return toString() + "  size=" + size();
    }
}
