package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22367946
 *
 * Splits the list in half, sorts each half, then merges the two together.
 *
 * The clever part is the merge. If you have two sorted lists, you can join
 * them into one sorted list by repeatedly taking whichever front item is
 * smaller. That takes one pass, so O(n).
 *
 * Splitting keeps halving the list, so there are log n levels, and each
 * level does O(n) work merging. That gives O(n log n) every single time,
 * whatever the data looks like. Quick sort is usually faster in practice but
 * can go bad; merge sort never does.
 *
 * The cost is memory. Merging needs somewhere to put the joined result, so
 * this uses an extra array of the same size. Insertion sort and quick sort
 * work in place. Experiment E2 shows the time difference; the memory
 * difference is worth a sentence in the report too.
 *
 * Speed: O(n log n) best, worst and average.
 *
 * Proof by induction, which this file owes the report:
 *
 *   Claim: merge sort returns the same items in sorted order.
 *   Base:  a list of 0 or 1 items is already sorted, and we return it as is.
 *   Step:  assume it sorts any list shorter than n. For a list of n we sort
 *          two shorter halves, so by the assumption both come back sorted.
 *          Merge takes the smaller front item each time, so it produces a
 *          sorted list holding exactly the items of both halves.
 *   So it holds for every length.
 */
public class MergeSort {

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order) {
        return run(items, order, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> AlgorithmResult run(T[] items, Comparator<T> order,
                                          boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long start = System.nanoTime();

        // One scratch array, made once and reused. Making a new one at every
        // level would be far slower.
        Object[] scratch = new Object[items.length];

        sortPart(items, scratch, 0, items.length - 1, order, result, withTrace, 0);

        result.setElapsedNanos(System.nanoTime() - start);
        result.setAnswer(items);
        return result;
    }

    private static <T> void sortPart(T[] items, Object[] scratch, int low, int high,
                                     Comparator<T> order, AlgorithmResult result,
                                     boolean withTrace, int depth) {
        if (low >= high) {
            return;     // 0 or 1 items, nothing to do
        }

        int middle = low + (high - low) / 2;

        sortPart(items, scratch, low, middle, order, result, withTrace, depth + 1);
        sortPart(items, scratch, middle + 1, high, order, result, withTrace, depth + 1);

        merge(items, scratch, low, middle, high, order, result);

        if (withTrace) {
            String[] extras = {low + ".." + high, String.valueOf(depth)};
            result.addTrace("merge " + low + ".." + middle + " with "
                            + (middle + 1) + ".." + high,
                    SelectionSort.show(items),
                    "this section is now sorted",
                    extras);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void merge(T[] items, Object[] scratch, int low, int middle,
                                  int high, Comparator<T> order, AlgorithmResult result) {

        // Copy the section we are about to merge into the scratch array.
        for (int i = low; i <= high; i++) {
            scratch[i] = items[i];
        }

        int left = low;             // where we are in the left half
        int right = middle + 1;     // where we are in the right half
        int write = low;            // where the next item goes

        while (left <= middle && right <= high) {
            result.countComparison();
            if (order.compare((T) scratch[left], (T) scratch[right]) <= 0) {
                items[write] = (T) scratch[left];
                left++;
            } else {
                items[write] = (T) scratch[right];
                right++;
            }
            result.countSwap();
            write++;
        }

        // One half may still have items. The other is empty by now.
        while (left <= middle) {
            items[write] = (T) scratch[left];
            result.countSwap();
            left++;
            write++;
        }
        while (right <= high) {
            items[write] = (T) scratch[right];
            result.countSwap();
            right++;
            write++;
        }
    }

    public static String[] traceColumns() {
        return new String[]{"Section", "Depth"};
    }
}
