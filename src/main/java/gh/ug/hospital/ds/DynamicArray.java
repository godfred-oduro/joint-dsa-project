package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22399601
 *
 * A list that grows by itself.
 *
 * A plain Java array has a fixed size. Once it is full you cannot add
 * anything. This class hides that: when the inside array fills up, we make a
 * new array twice as big, copy everything across, and carry on. The person
 * using the class never sees it happen.
 *
 * Speed:
 *   add at the end       O(1) most of the time, O(n) on the run that grows
 *   get and set          O(1)
 *   add or remove middle O(n), because everything after it shifts
 *   contains             O(n)
 */
public class DynamicArray<T> {

    /** How big the inside array is when nobody says otherwise. */
    private static final int STARTING_CAPACITY = 8;

    private Object[] items;
    private int count;

    /** How many times the array has grown. Used for the resize trace. */
    private int growCount;

    public DynamicArray() {
        this(STARTING_CAPACITY);
    }

    public DynamicArray(int startingCapacity) {
        if (startingCapacity <= 0) {
            throw new StructureException(
                    "A dynamic array needs room for at least 1 item, but got "
                            + startingCapacity);
        }
        this.items = new Object[startingCapacity];
        this.count = 0;
        this.growCount = 0;
    }

    /** Puts an item on the end. */
    public void add(T item) {
        growIfFull();
        items[count] = item;
        count++;
    }

    /** Puts an item in the middle, pushing everything after it along. */
    public void addAt(int index, T item) {
        // Note index == count is allowed here. That just means "add at the end".
        if (index < 0 || index > count) {
            throw new StructureException(
                    "Cannot add at position " + index + ". The array holds " + count + " items.");
        }
        growIfFull();

        // Walk backwards so we do not overwrite an item before we have moved it.
        for (int i = count; i > index; i--) {
            items[i] = items[i - 1];
        }
        items[index] = item;
        count++;
    }

    /** Reads the item at a position. */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) items[index];
    }

    /** Replaces the item at a position and hands back the old one. */
    @SuppressWarnings("unchecked")
    public T set(int index, T item) {
        checkIndex(index);
        T old = (T) items[index];
        items[index] = item;
        return old;
    }

    /** Takes out the item at a position, pulling everything after it back. */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkIndex(index);
        T removed = (T) items[index];

        for (int i = index; i < count - 1; i++) {
            items[i] = items[i + 1];
        }

        // Clear the last slot so the old object can be tidied away by Java.
        items[count - 1] = null;
        count--;
        return removed;
    }

    /** True if the item is somewhere in the array. */
    public boolean contains(T item) {
        return indexOf(item) >= 0;
    }

    /** Where the item sits, or -1 if it is not there. */
    public int indexOf(T item) {
        for (int i = 0; i < count; i++) {
            if (item == null) {
                if (items[i] == null) {
                    return i;
                }
            } else if (item.equals(items[i])) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    /** How much room the inside array has right now. */
    public int capacity() {
        return items.length;
    }

    /** How many times the array has doubled. Used in the resize trace. */
    public int getGrowCount() {
        return growCount;
    }

    public void clear() {
        for (int i = 0; i < count; i++) {
            items[i] = null;
        }
        count = 0;
    }

    /** Hands back a plain array holding exactly the items, in order. */
    public Object[] toArray() {
        Object[] exact = new Object[count];
        for (int i = 0; i < count; i++) {
            exact[i] = items[i];
        }
        return exact;
    }

    // ---- the part that does the growing ----

    private void growIfFull() {
        if (count < items.length) {
            return;
        }
        Object[] bigger = new Object[items.length * 2];
        for (int i = 0; i < items.length; i++) {
            bigger[i] = items[i];
        }
        items = bigger;
        growCount++;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= count) {
            throw new StructureException(
                    "Position " + index + " does not exist. The array holds "
                            + count + " items.");
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(items[i]);
        }
        out.append("]");
        return out.toString();
    }

    /** Short line for a trace table: contents, how full, and how many grows. */
    public String toTraceString() {
        return toString() + "  size=" + count + "/" + items.length
                + "  grows=" + growCount;
    }
}
