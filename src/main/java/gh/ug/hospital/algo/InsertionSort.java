package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22370298
 *
 * Sorts the way most people sort a hand of playing cards.
 *
 * Take the next card. Slide it left past every card bigger than it. Drop it
 * in. Repeat. Everything to the left of where you are working stays sorted
 * the whole time.
 *
 * Unlike selection sort, this one notices when the data is already in good
 * order. On a sorted list it makes one comparison per item and no moves at
 * all, so it finishes in O(n). That makes it the fastest of the four on
 * nearly-sorted data, and it is why real sorting libraries switch to
 * insertion sort once the pieces get small.
 *
 * Speed:
 *   best case     O(n), already sorted
 *   worst case    O(n squared), sorted backwards
 *
 * The loop invariant, which this file owes as a proof sketch for the report:
 *
 *   Before each pass with key at position i, the items in positions
 *   0 to i-1 are the same items that started there, now in sorted order.
 *
 *   Start:  i = 1, so positions 0 to 0 hold one item, trivially sorted.
 *   Step:   the pass slides everything bigger than the key one place right,
 *           then drops the key into the gap. Nothing else moves, and the key
 *           lands after everything smaller and before everything bigger, so
 *           positions 0 to i are sorted.
 *   End:    the loop stops when i reaches n, so positions 0 to n-1, the
 *           whole list, are sorted. That is what we wanted.
 */
public class InsertionSort {

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order) {
        return run(items, order, false);
    }

    public static <T> AlgorithmResult run(T[] items, Comparator<T> order,
                                          boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long start = System.nanoTime();

        for (int i = 1; i < items.length; i++) {
            T key = items[i];
            int j = i - 1;
            int shifts = 0;

            // Slide everything bigger than the key one place to the right.
            while (j >= 0) {
                result.countComparison();
                if (order.compare(items[j], key) <= 0) {
                    break;
                }
                items[j + 1] = items[j];
                result.countSwap();
                shifts++;
                j--;
            }

            items[j + 1] = key;

            if (withTrace) {
                String[] extras = {String.valueOf(key), String.valueOf(shifts)};
                result.addTrace("place item from position " + i,
                        SelectionSort.show(items),
                        "positions 0 to " + i + " are sorted",
                        extras);
            }
        }

        result.setElapsedNanos(System.nanoTime() - start);
        result.setAnswer(items);
        return result;
    }

    /** Extra trace columns, on top of the standard six. */
    public static String[] traceColumns() {
        return new String[]{"Key", "Shift Count"};
    }
}
