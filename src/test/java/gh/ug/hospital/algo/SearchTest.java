package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.exception.DomainException;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owners: 22305896 (linear), 22392509 (binary)
 *
 * Tests for the two searches.
 *
 * The last test here is one of the two counterexamples the brief asks for.
 * It shows that binary search on unsorted data does not merely go slower.
 * It returns the wrong answer.
 */
/**
 * Owner: 22392509
 *
 * Tests for both searches, including the unsorted counterexample.
 */
public class SearchTest {

    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    private Integer[] numbers(int... values) {
        Integer[] boxed = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            boxed[i] = Integer.valueOf(values[i]);
        }
        return boxed;
    }

    private int answerOf(AlgorithmResult result) {
        return ((Integer) result.getAnswer()).intValue();
    }

    @Test
    public void ordinaryUse_bothSearchesFindTheSameThing() {
        Integer[] sorted = numbers(10, 20, 30, 40, 50);
        Integer target = Integer.valueOf(40);

        AlgorithmResult linear = LinearSearch.run(sorted, target, BY_VALUE);
        AlgorithmResult binary = BinarySearch.run(sorted, target, BY_VALUE);

        assertEquals(3, answerOf(linear));
        assertEquals(3, answerOf(binary));
    }

    @Test
    public void ordinaryUse_bothReportMinusOneWhenItIsNotThere() {
        Integer[] sorted = numbers(10, 20, 30);
        Integer missing = Integer.valueOf(99);

        assertEquals(-1, answerOf(LinearSearch.run(sorted, missing, BY_VALUE)));
        assertEquals(-1, answerOf(BinarySearch.run(sorted, missing, BY_VALUE)));
    }

    @Test
    public void ordinaryUse_binarySearchDoesFarLessWork() {
        // This is the whole point of experiment E1. Binary search throws away
        // half the data at every step, so it should need very few comparisons
        // even when the item is at the very end.
        Integer[] sorted = new Integer[1000];
        for (int i = 0; i < 1000; i++) {
            sorted[i] = Integer.valueOf(i);
        }
        Integer last = Integer.valueOf(999);

        AlgorithmResult linear = LinearSearch.run(sorted, last, BY_VALUE);
        AlgorithmResult binary = BinarySearch.run(sorted, last, BY_VALUE);

        assertEquals(1000, linear.getComparisons());
        assertTrue(binary.getComparisons() <= 12,
                "binary search used " + binary.getComparisons()
                        + " comparisons, which is more than log2(1000) allows");
    }

    @Test
    public void badInput_binarySearchRefusesUnsortedData() {
        // Binary search works by throwing away half the data at every step,
        // which only makes sense if the data is sorted. On unsorted data it
        // does not go slow, it goes wrong, so we check before searching.
        final Integer[] unsorted = {
            Integer.valueOf(50), Integer.valueOf(20), Integer.valueOf(90)
        };

        assertThrows(DomainException.class, new Executable() {
            public void execute() {
                BinarySearch.run(unsorted, Integer.valueOf(20), BY_VALUE);
            }
        });
    }
}
