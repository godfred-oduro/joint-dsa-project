package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22372680
 *
 * Sorts by repeatedly picking the smallest item left and putting it next.
 *
 * Pass one looks at the whole list and finds the smallest, then swaps it into
 * position 0. Pass two looks at everything from position 1 onward and swaps
 * the smallest of those into position 1. And so on.
 *
 * The comparison count never changes, whatever the data. Sorted, reversed or
 * random, it always makes n(n-1)/2 comparisons, because it always scans the
 * whole remaining list. That makes it a good baseline in experiment E2: the
 * flattest, most predictable line on the graph.
 *
 * Its one small virtue is that it makes at most n-1 swaps, which matters if
 * moving items is expensive.
 *
 * Speed: O(n squared) always, best case and worst case alike.
 */
public class SelectionSort {

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order) {
        return run(items, order, false);
    }

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order,
                                          boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long start = System.nanoTime();

        for (int position = 0; position < items.length - 1; position++) {

            // Assume the item already here is the smallest, then look for better.
            int smallest = position;
            for (int scan = position + 1; scan < items.length; scan++) {
                result.countComparison();
                if (order.compare(items[scan], items[smallest]) < 0) {
                    smallest = scan;
                }
            }

            if (smallest != position) {
                T hold = items[position];
                items[position] = items[smallest];
                items[smallest] = hold;
                result.countSwap();
            }

            if (withTrace) {
                result.addTrace("pass " + (position + 1)
                                + ": smallest of the rest was at " + smallest,
                        show(items),
                        "positions 0 to " + position + " are now final");
            }
        }

        result.setElapsedNanos(System.nanoTime() - start);
        result.setAnswer(items);
        return result;
    }

    static <T> String show(T[] items) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(items[i]);
        }
        out.append("]");
        return out.toString();
    }
}
