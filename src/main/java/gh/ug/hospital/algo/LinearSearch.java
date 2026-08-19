package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22305896
 *
 * Look through everything one item at a time until you find it.
 *
 * This is the simplest search there is. It does not care whether the data is
 * sorted, which is its one advantage over binary search. Its disadvantage is
 * that with ten thousand items it may look at all ten thousand.
 *
 * Experiment E1 puts this next to binary search, and the gap between the two
 * lines is the whole lesson.
 *
 * Speed:
 *   best case     O(1), the item is first
 *   worst case    O(n), the item is last or missing
 *   average       O(n/2), which we still call O(n)
 */
public class LinearSearch {

    /**
     * Finds where the item sits, or -1 if it is not there.
     *
     * @param items  the data to look through
     * @param target what we are looking for
     * @param order  how to tell whether two items are the same
     */
    public static <T> AlgorithmResult run(T[] items, T target, Comparator<T> order) {
        AlgorithmResult result = new AlgorithmResult();
        long startedAt = System.nanoTime();

        int found = -1;

        for (int i = 0; i < items.length; i++) {
            result.countComparison();

            if (order.compare(items[i], target) == 0) {
                found = i;
                result.addTrace(
                        "compare position " + i,
                        String.valueOf(items[i]),
                        "match found");
                break;
            }

            // Only record the first few steps, otherwise a trace of 10000
            // rows is useless to anybody reading the report.
            if (i < 10) {
                result.addTrace(
                        "compare position " + i,
                        String.valueOf(items[i]),
                        "no match, keep going");
            }
        }

        if (found < 0) {
            result.addTrace("reached the end", "-", "item is not in the data");
        }

        result.setElapsedNanos(System.nanoTime() - startedAt);
        result.setAnswer(Integer.valueOf(found));
        return result;
    }
}
