package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22224236
 *
 * Tests for BinaryHeap, the structure that decides who is seen next.
 */
public class BinaryHeapTest {

    /** Compares numbers the ordinary way. */
    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    @Test
    public void ordinaryUse_smallestComesOutFirstWhateverTheOrderGoingIn() {
        BinaryHeap<Integer> heap = new BinaryHeap<>(BY_VALUE);
        heap.insert(Integer.valueOf(50));
        heap.insert(Integer.valueOf(10));
        heap.insert(Integer.valueOf(30));
        heap.insert(Integer.valueOf(5));

        assertEquals(Integer.valueOf(5), heap.extract());
        assertEquals(Integer.valueOf(10), heap.extract());
        assertEquals(Integer.valueOf(30), heap.extract());
        assertEquals(Integer.valueOf(50), heap.extract());
        assertTrue(heap.isEmpty());
    }

    @Test
    public void ordinaryUse_peekCostsNothingAndRemovesNothing() {
        BinaryHeap<Integer> heap = new BinaryHeap<>(BY_VALUE);
        heap.insert(Integer.valueOf(7));
        heap.insert(Integer.valueOf(2));

        assertEquals(Integer.valueOf(2), heap.peek());
        assertEquals(2, heap.size());
    }

    @Test
    public void edge_buildingFromAWholeArrayGivesTheSameOrder() {
        Integer[] numbers = {
            Integer.valueOf(9), Integer.valueOf(4), Integer.valueOf(7),
            Integer.valueOf(1), Integer.valueOf(8)
        };

        BinaryHeap<Integer> heap = new BinaryHeap<>(BY_VALUE);
        heap.heapify(numbers);

        assertEquals(5, heap.size());
        assertEquals(Integer.valueOf(1), heap.extract());
        assertEquals(Integer.valueOf(4), heap.extract());
    }

    @Test
    public void badInput_takingFromAnEmptyHeapFails() {
        final BinaryHeap<Integer> empty = new BinaryHeap<>(BY_VALUE);

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.extract();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.peek();
            }
        });
    }
}
