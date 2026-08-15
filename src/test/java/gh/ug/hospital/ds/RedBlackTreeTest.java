package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22040255
 *
 * Tests for RedBlackTree. The important one shows it staying short where a plain tree would not.
 */
public class RedBlackTreeTest {

    /** Compares numbers the ordinary way. */
    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    @Test
    public void ordinaryUse_inorderComesOutSorted() {
        RedBlackTree<Integer> tree = new RedBlackTree<>(BY_VALUE);
        tree.insert(Integer.valueOf(50));
        tree.insert(Integer.valueOf(20));
        tree.insert(Integer.valueOf(80));

        DynamicArray<Integer> sorted = tree.inorder();
        assertEquals(Integer.valueOf(20), sorted.get(0));
        assertEquals(Integer.valueOf(50), sorted.get(1));
        assertEquals(Integer.valueOf(80), sorted.get(2));
    }

    @Test
    public void ordinaryUse_staysShortOnSortedInput() {
        // The same sorted input that turned the plain tree into a chain of
        // eight. Here the rotations keep it short. This pair of tests is the
        // evidence behind experiment E4.
        RedBlackTree<Integer> tree = new RedBlackTree<>(BY_VALUE);
        for (int i = 1; i <= 8; i++) {
            tree.insert(Integer.valueOf(i));
        }

        assertEquals(8, tree.size());
        assertTrue(tree.height() <= 5,
                "height was " + tree.height() + ", which is too tall for 8 items");
        assertTrue(tree.getRotations() > 0, "no rotations happened at all");
    }

    @Test
    public void edge_everyPathHasTheSameNumberOfBlackNodes() {
        // Rule 5 of a red-black tree. If this breaks, the tree is not balanced.
        RedBlackTree<Integer> tree = new RedBlackTree<>(BY_VALUE);
        for (int i = 1; i <= 20; i++) {
            tree.insert(Integer.valueOf(i * 3));
        }

        assertTrue(tree.blackHeight() > 0);
        assertTrue(tree.height() <= 2 * tree.blackHeight() + 1);
    }

    @Test
    public void badInput_duplicatesAndMissingKeysAreRefused() {
        final RedBlackTree<Integer> tree = new RedBlackTree<>(BY_VALUE);
        tree.insert(Integer.valueOf(1));

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                tree.insert(Integer.valueOf(1));
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                tree.delete(Integer.valueOf(999));
            }
        });
    }
}
