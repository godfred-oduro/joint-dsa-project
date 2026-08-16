package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

import java.util.Comparator;

/**
 * Owner: 22224236
 *
 * A heap, which is how we always get the most urgent patient next.
 *
 * The idea: keep the items in an array, but arranged as a tree. Position 0
 * is the root. For any position i, its two children sit at 2i+1 and 2i+2,
 * and its parent sits at (i-1)/2. The rule is that a parent always comes
 * before its children in priority order. The root is therefore always the
 * most urgent item, and reading it costs nothing.
 *
 * When we add an item we put it at the end and let it "swim up" past any
 * parent it should beat. When we remove the root we move the last item into
 * its place and let it "sink down" past any child that beats it.
 *
 * This is the structure behind rule BR1, the order in which patients are seen.
 *
 * Named BinaryHeap because java.util already has PriorityQueue.
 *
 * Speed:
 *   peek                    O(1)
 *   insert and extract      O(log n)
 *   heapify a whole array   O(n)
 *
 * Note: java.util.Comparator is an interface for comparing two things.
 * It is not a collection, so using it does not break constraint C19.
 */
public class BinaryHeap<T> {

    private Object[] items;
    private int count;
    private final Comparator<T> order;

    /** How many times we swapped. Useful for the dispatch-order trace. */
    private long swapCount;

    public BinaryHeap(Comparator<T> order) {
        this(order, 16);
    }

    public BinaryHeap(Comparator<T> order, int startingCapacity) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "A heap needs a rule for deciding which item comes first.");
        }
        if (startingCapacity < 1) {
            startingCapacity = 1;
        }
        this.order = order;
        this.items = new Object[startingCapacity];
        this.count = 0;
        this.swapCount = 0;
    }

    /** Adds an item, then lets it swim up to where it belongs. */
    public void insert(T item) {
        growIfFull();
        items[count] = item;
        count++;
        swimUp(count - 1);
    }

    /** Looks at the most urgent item without removing it. */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot peek: the heap is empty. Nobody is waiting.");
        }
        return (T) items[0];
    }

    /** Takes the most urgent item out and hands it back. */
    @SuppressWarnings("unchecked")
    public T extract() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot extract: the heap is empty.");
        }
        T best = (T) items[0];

        // Move the last item to the root, shrink, then let it sink.
        items[0] = items[count - 1];
        items[count - 1] = null;
        count--;
        if (count > 0) {
            sinkDown(0);
        }
        return best;
    }

    /**
     * Builds a heap out of a whole array in one go.
     *
     * Doing insert() n times would cost O(n log n). Sinking each parent from
     * the middle backwards costs only O(n), which is worth knowing for the
     * report.
     */
    public void heapify(T[] source) {
        items = new Object[Math.max(source.length, 1)];
        for (int i = 0; i < source.length; i++) {
            items[i] = source[i];
        }
        count = source.length;

        // Everything past the halfway point is a leaf and has nothing to sink past.
        for (int i = count / 2 - 1; i >= 0; i--) {
            sinkDown(i);
        }
    }

    // ---- the two movements that keep the heap correct ----

    @SuppressWarnings("unchecked")
    private void swimUp(int position) {
        while (position > 0) {
            int parent = (position - 1) / 2;
            // If the parent already comes first, we are done.
            if (order.compare((T) items[position], (T) items[parent]) >= 0) {
                break;
            }
            swap(position, parent);
            position = parent;
        }
    }

    @SuppressWarnings("unchecked")
    private void sinkDown(int position) {
        while (true) {
            int left = 2 * position + 1;
            int right = 2 * position + 2;
            int best = position;

            if (left < count
                    && order.compare((T) items[left], (T) items[best]) < 0) {
                best = left;
            }
            if (right < count
                    && order.compare((T) items[right], (T) items[best]) < 0) {
                best = right;
            }
            if (best == position) {
                break;      // both children already come after this one
            }
            swap(position, best);
            position = best;
        }
    }

    private void swap(int a, int b) {
        Object hold = items[a];
        items[a] = items[b];
        items[b] = hold;
        swapCount++;
    }

    private void growIfFull() {
        if (count < items.length) {
            return;
        }
        Object[] bigger = new Object[items.length * 2];
        for (int i = 0; i < items.length; i++) {
            bigger[i] = items[i];
        }
        items = bigger;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public long getSwapCount() {
        return swapCount;
    }

    public void clear() {
        for (int i = 0; i < count; i++) {
            items[i] = null;
        }
        count = 0;
        swapCount = 0;
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

    public String toTraceString() {
        return toString() + "  size=" + count + "  swaps=" + swapCount;
    }
}
