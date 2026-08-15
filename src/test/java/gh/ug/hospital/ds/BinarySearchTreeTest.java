package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22305896
 *
 * Tests for BinarySearchTree, including the case that shows why balancing matters.
 */
public class BinarySearchTreeTest {

    /** Compares numbers the ordinary way. */
    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    @Test
    public void ordinaryUse_inorderComesOutSorted() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>(BY_VALUE);
        tree.insert(Integer.valueOf(50));
        tree.insert(Integer.valueOf(30));
        tree.insert(Integer.valueOf(70));
        tree.insert(Integer.valueOf(20));

        DynamicArray<Integer> sorted = tree.inorder();
        assertEquals(4, sorted.size());
        assertEquals(Integer.valueOf(20), sorted.get(0));
        assertEquals(Integer.valueOf(30), sorted.get(1));
        assertEquals(Integer.valueOf(50), sorted.get(2));
        assertEquals(Integer.valueOf(70), sorted.get(3));
    }

    @Test
    public void ordinaryUse_findsWhatIsThereAndNotWhatIsNot() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>(BY_VALUE);
        tree.insert(Integer.valueOf(10));
        tree.insert(Integer.valueOf(5));

        assertTrue(tree.contains(Integer.valueOf(5)));
        assertFalse(tree.contains(Integer.valueOf(999)));
        assertNull(tree.search(Integer.valueOf(999)));
    }

    @Test
    public void edge_sortedInputTurnsTheTreeIntoALongChain() {
        // This is the whole reason the red-black tree exists. Feed a plain
        // search tree data that is already sorted and every item goes right,
        // so the height equals the number of items and searching is no
        // better than walking a list. Experiment E4 measures this.
        BinarySearchTree<Integer> lopsided = new BinarySearchTree<>(BY_VALUE);
        for (int i = 1; i <= 8; i++) {
            lopsided.insert(Integer.valueOf(i));
        }

        assertEquals(8, lopsided.size());
        assertEquals(8, lopsided.height());
    }

    @Test
    public void badInput_duplicatesAndNullsAreRefused() {
        final BinarySearchTree<Integer> tree = new BinarySearchTree<>(BY_VALUE);
        tree.insert(Integer.valueOf(42));

        // Every location code and request code in this project is unique, so
        // a duplicate means the data is wrong rather than merely unusual.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                tree.insert(Integer.valueOf(42));
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                tree.insert(null);
            }
        });
    }
}
