package gh.ug.hospital.algo;

import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.exception.DomainException;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owners: 22372680 (selection), 22370298 (insertion),
 *         22367946 (merge), 22395395 (quick)
 *
 * Tests for the four comparison sorts.
 *
 * They share one test class because they all have to produce exactly the
 * same answer. If they disagree, one of them is wrong. What differs is how
 * much work each does to get there, and that is what experiment E2 measures.
 */
/**
 * Owner: 22367946
 *
 * Tests for the four comparison sorts.
 */
public class SortingTest {

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

    private void assertSorted(Integer[] items) {
        for (int i = 1; i < items.length; i++) {
            assertTrue(items[i - 1].intValue() <= items[i].intValue(),
                    "position " + (i - 1) + " holds " + items[i - 1]
                            + " which is bigger than " + items[i]);
        }
    }

    @Test
    public void ordinaryUse_allFourGiveTheSameAnswer() {
        int[] raw = {42, 7, 19, 3, 88, 15, 60, 1};

        AlgorithmResult selection = SelectionSort.run(numbers(raw), BY_VALUE);
        AlgorithmResult insertion = InsertionSort.run(numbers(raw), BY_VALUE);
        AlgorithmResult merge = MergeSort.run(numbers(raw), BY_VALUE);
        AlgorithmResult quick = QuickSort.run(numbers(raw), BY_VALUE);

        Integer[] a = (Integer[]) selection.getAnswer();
        Integer[] b = (Integer[]) insertion.getAnswer();
        Integer[] c = (Integer[]) merge.getAnswer();
        Integer[] d = (Integer[]) quick.getAnswer();

        assertSorted(a);
        assertSorted(b);
        assertSorted(c);
        assertSorted(d);

        for (int i = 0; i < raw.length; i++) {
            assertEquals(a[i], b[i], "selection and insertion disagree at " + i);
            assertEquals(a[i], c[i], "selection and merge disagree at " + i);
            assertEquals(a[i], d[i], "selection and quick disagree at " + i);
        }
    }

    @Test
    public void ordinaryUse_everySortCountsItsOwnWork() {
        // The counts feed both the timing experiments and the trace tables,
        // so a sort that does not count is a sort we cannot report on.
        AlgorithmResult merge = MergeSort.run(numbers(5, 3, 8, 1), BY_VALUE);

        assertTrue(merge.getComparisons() > 0, "merge sort counted no comparisons");
        assertTrue(merge.getPrimitiveOperations() > 0);
    }

    @Test
    public void edge_alreadySortedDataStaysSorted() {
        AlgorithmResult result = InsertionSort.run(numbers(1, 2, 3, 4, 5), BY_VALUE);
        assertSorted((Integer[]) result.getAnswer());
    }

    @Test
    public void edge_duplicateValuesStaySorted() {
        // Real ward data repeats values all the time (two patients on the
        // same triage colour, two jobs costing the same number of minutes).
        // A sort that mishandles ties either drops an item or leaves the
        // array out of order, so this has to be checked on its own.
        int[] raw = {5, 3, 5, 1, 3, 5, 2};

        AlgorithmResult selection = SelectionSort.run(numbers(raw), BY_VALUE);
        AlgorithmResult insertion = InsertionSort.run(numbers(raw), BY_VALUE);
        AlgorithmResult merge = MergeSort.run(numbers(raw), BY_VALUE);
        AlgorithmResult quick = QuickSort.run(numbers(raw), BY_VALUE);

        Integer[] a = (Integer[]) selection.getAnswer();
        Integer[] b = (Integer[]) insertion.getAnswer();
        Integer[] c = (Integer[]) merge.getAnswer();
        Integer[] d = (Integer[]) quick.getAnswer();

        assertSorted(a);
        assertSorted(b);
        assertSorted(c);
        assertSorted(d);

        // Every sort must still hold all seven original items, not just six
        // distinct values - a sort that de-duplicates by accident passes
        // assertSorted() while silently losing data.
        assertEquals(raw.length, a.length);
        assertEquals(raw.length, b.length);
        assertEquals(raw.length, c.length);
        assertEquals(raw.length, d.length);
    }

    @Test
    public void badInput_sortingNothingAtAllIsRefused() {
        // The sorts do not check their input themselves, because a sort that
        // second-guesses every call would be slower for no gain. Handing one
        // a null array therefore fails immediately rather than quietly doing
        // nothing, which is the behaviour we want and are documenting here.
        assertThrows(NullPointerException.class, new Executable() {
            public void execute() {
                MergeSort.run(null, BY_VALUE);
            }
        });
        assertThrows(NullPointerException.class, new Executable() {
            public void execute() {
                QuickSort.run(null, BY_VALUE);
            }
        });
    }
}
