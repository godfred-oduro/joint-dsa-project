package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22395395
 *
 * Picks one item as the pivot, puts everything smaller to its left and
 * everything bigger to its right, then does the same to each side.
 *
 * Once an item has been used as a pivot and everything has been shuffled
 * around it, that item is in its final place forever. The two sides can then
 * be sorted separately.
 *
 * On paper this is O(n log n), the same as merge sort. In practice it is
 * usually faster, because it works inside the original array with no scratch
 * space, so the processor cache is used well. Experiment E2 normally shows
 * quick sort beating merge sort despite identical Big-O, and explaining that
 * gap is worth marks in section 10.
 *
 * The danger: if the pivot is always the smallest or largest item, one side
 * is empty every time and it degrades to O(n squared). Picking the pivot as
 * the middle of first, middle and last makes that very unlikely, and stops
 * already-sorted data from being the worst case.
 *
 * Speed:
 *   average    O(n log n)
 *   worst      O(n squared), with a bad run of pivots
 */
public class QuickSort {

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order) {
        return run(items, order, false);
    }

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order,
                                          boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long start = System.nanoTime();

        sortPart(items, 0, items.length - 1, order, result, withTrace, 0);

        result.setElapsedNanos(System.nanoTime() - start);
        result.setAnswer(items);
        return result;
    }

    private static <T> void sortPart(T[] items, int low, int high,
                                     Comparator<T> order, AlgorithmResult result,
                                     boolean withTrace, int depth) {
        if (low >= high) {
            return;
        }

        int pivotPosition = partition(items, low, high, order, result);

        if (withTrace) {
            String[] extras = {
                    low + ".." + high,
                    String.valueOf(items[pivotPosition]),
                    String.valueOf(depth)
            };
            result.addTrace("partition " + low + ".." + high,
                    SelectionSort.show(items),
                    "the pivot is now settled at position " + pivotPosition,
                    extras);
        }

        sortPart(items, low, pivotPosition - 1, order, result, withTrace, depth + 1);
        sortPart(items, pivotPosition + 1, high, order, result, withTrace, depth + 1);
    }

    /**
     * Shuffles the section so everything smaller than the pivot sits before
     * it and everything bigger sits after it. Hands back where the pivot
     * ended up.
     */
    private static <T> int partition(T[] items, int low, int high,
                                     Comparator<T> order, AlgorithmResult result) {

        choosePivot(items, low, high, order, result);
        T pivot = items[high];

        int boundary = low - 1;     // last position known to hold a smaller item

        for (int scan = low; scan < high; scan++) {
            result.countComparison();
            if (order.compare(items[scan], pivot) <= 0) {
                boundary++;
                swap(items, boundary, scan, result);
            }
        }

        // Put the pivot just past the smaller items.
        swap(items, boundary + 1, high, result);
        return boundary + 1;
    }

    /**
     * Looks at the first, middle and last item, and moves the middle-sized
     * one of the three to the end to be used as the pivot.
     *
     * Without this, already-sorted data would be the worst case, which is
     * exactly the sort of data a hospital list often is.
     */
    private static <T> void choosePivot(T[] items, int low, int high,
                                        Comparator<T> order, AlgorithmResult result) {
        int middle = low + (high - low) / 2;

        result.addComparisons(3);

        if (order.compare(items[middle], items[low]) < 0) {
            swap(items, low, middle, result);
        }
        if (order.compare(items[high], items[low]) < 0) {
            swap(items, low, high, result);
        }
        if (order.compare(items[high], items[middle]) < 0) {
            swap(items, middle, high, result);
        }
        // items[middle] now holds the middle-sized of the three. Move it to
        // the end, which is where partition expects the pivot to be.
        swap(items, middle, high, result);
    }

    private static <T> void swap(T[] items, int a, int b, AlgorithmResult result) {
        if (a == b) {
            return;
        }
        T hold = items[a];
        items[a] = items[b];
        items[b] = hold;
        result.countSwap();
    }

    public static String[] traceColumns() {
        return new String[]{"Section", "Pivot", "Depth"};
    }
}
