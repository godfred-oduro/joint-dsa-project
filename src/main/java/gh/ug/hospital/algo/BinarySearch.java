package gh.ug.hospital.algo;

import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22392509
 *
 * Look in the middle, then throw away the half that cannot contain the item.
 *
 * Because half the data disappears at every step, ten thousand items take
 * about fourteen comparisons instead of ten thousand. That is the difference
 * between O(n) and O(log n), and experiment E1 shows it clearly.
 *
 * The catch is that the data must already be sorted. On unsorted data the
 * method does not just go slower, it gives the wrong answer, because the
 * half it throws away may be the half holding the item.
 *
 * That is our second required counterexample. The method checkSorted below
 * proves the point on purpose, and menu option M6.9 demonstrates it.
 *
 * Speed:
 *   best case     O(1), the item is exactly in the middle
 *   worst case    O(log n)
 */
public class BinarySearch {

    /**
     * Searches sorted data. Complains if the data is not sorted.
     */
    public static <T> AlgorithmResult run(T[] items, T target, Comparator<T> order) {
        checkSorted(items, order);
        return searchWithoutChecking(items, target, order);
    }

    /**
     * The same search, but with no sorted check.
     *
     * This exists so we can show what goes wrong. Menu option M6.9 hands it
     * unsorted data and shows it reporting "not found" for an item that is
     * plainly sitting in the array.
     */
    public static <T> AlgorithmResult searchWithoutChecking(T[] items, T target,
                                                            Comparator<T> order) {
        AlgorithmResult result = new AlgorithmResult();
        long startedAt = System.nanoTime();

        int low = 0;
        int high = items.length - 1;
        int found = -1;

        while (low <= high) {
            // Written this way rather than (low + high) / 2 because on very
            // large arrays that version can overflow past the int limit.
            int middle = low + (high - low) / 2;

            result.countComparison();
            int side = order.compare(target, items[middle]);

            String[] extras = {
                    String.valueOf(low),
                    String.valueOf(high),
                    String.valueOf(middle),
                    String.valueOf(items[middle])
            };

            if (side == 0) {
                result.addTrace("compare with middle", String.valueOf(items[middle]),
                        "match found", extras);
                found = middle;
                break;
            }

            if (side < 0) {
                result.addTrace("compare with middle", String.valueOf(items[middle]),
                        "target is smaller, drop the right half", extras);
                high = middle - 1;
            } else {
                result.addTrace("compare with middle", String.valueOf(items[middle]),
                        "target is bigger, drop the left half", extras);
                low = middle + 1;
            }
        }

        if (found < 0) {
            result.addTrace("low passed high", "-", "item is not in the data",
                    new String[]{String.valueOf(low), String.valueOf(high), "-", "-"});
        }

        result.setElapsedNanos(System.nanoTime() - startedAt);
        result.setAnswer(Integer.valueOf(found));
        return result;
    }

    /** The extra column titles this algorithm needs in its trace table. */
    public static String[] traceExtraHeaders() {
        return new String[]{"Low", "High", "Mid", "Value at Mid"};
    }

    /**
     * Walks the array once and complains if any item is smaller than the one
     * before it.
     */
    public static <T> void checkSorted(T[] items, Comparator<T> order) {
        for (int i = 1; i < items.length; i++) {
            if (order.compare(items[i - 1], items[i]) > 0) {
                throw new DomainException(
                        "Binary search needs sorted data, but position "
                                + (i - 1) + " holds " + items[i - 1]
                                + " which is bigger than " + items[i]
                                + " at position " + i + ".");
            }
        }
    }
}
