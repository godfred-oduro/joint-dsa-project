package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22104879
 *
 * Tests for BTree, the wide tree that real databases use for their indexes.
 */
public class BTreeTest {

    /** Compares numbers the ordinary way. */
    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    @Test
    public void ordinaryUse_holdsAndFindsKeys() {
        BTree<Integer> tree = new BTree<>(4, BY_VALUE);
        tree.insert(Integer.valueOf(10));
        tree.insert(Integer.valueOf(20));
        tree.insert(Integer.valueOf(5));

        assertEquals(3, tree.size());
        assertTrue(tree.contains(Integer.valueOf(20)));
        assertNull(tree.search(Integer.valueOf(999)));
    }

    @Test
    public void ordinaryUse_traverseComesOutSorted() {
        BTree<Integer> tree = new BTree<>(3, BY_VALUE);
        int[] values = {40, 10, 30, 20, 50};
        for (int i = 0; i < values.length; i++) {
            tree.insert(Integer.valueOf(values[i]));
        }

        DynamicArray<Integer> sorted = tree.traverse();
        assertEquals(5, sorted.size());
        for (int i = 1; i < sorted.size(); i++) {
            assertTrue(sorted.get(i - 1).intValue() < sorted.get(i).intValue(),
                    "the keys came out in the wrong order");
        }
    }

    @Test
    public void edge_nodesSplitWhenTheyFillUp() {
        // With order 3 a node holds two keys, so the third one forces a split.
        // The split is the evidence this structure owes to the report.
        BTree<Integer> tree = new BTree<>(3, BY_VALUE);
        for (int i = 1; i <= 10; i++) {
            tree.insert(Integer.valueOf(i));
        }

        assertTrue(tree.getSplits() > 0, "no node ever split");
        assertTrue(tree.height() > 1, "the tree never grew past one level");
        assertEquals(10, tree.size());
    }

    @Test
    public void badInput_orderBelowThreeAndDuplicatesAreRefused() {
        // An order of 2 would just be a binary tree, which defeats the point
        // of a B-tree, so the constructor refuses it.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                new BTree<Integer>(2, BY_VALUE);
            }
        });

        final BTree<Integer> tree = new BTree<>(4, BY_VALUE);
        tree.insert(Integer.valueOf(7));
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                tree.insert(Integer.valueOf(7));
            }
        });
    }
}
