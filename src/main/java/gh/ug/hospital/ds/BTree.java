package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

import java.util.Comparator;

/**
 * Owner: 22104879
 *
 * A B-tree. A search tree where each node holds several keys, not just one.
 *
 * Binary trees hold one key per node, so a tree of a million items is about
 * twenty levels deep. That is fine in memory. It is terrible on a disk,
 * because every level is a separate read from the drive, and drive reads are
 * thousands of times slower than memory reads.
 *
 * A B-tree fixes this by making each node wide. If a node holds a hundred
 * keys, the same million items fit in three levels, so three disk reads
 * instead of twenty. This is why real databases, including the MySQL one
 * behind this project, use B-trees for their indexes.
 *
 * The "order" is the most children a node may have. A node holds at most
 * order-1 keys. When a node fills up it splits down the middle and the
 * middle key moves up to the parent. That split is the interesting part, and
 * it is the evidence this structure owes to the report.
 *
 * Speed: search, insert and traverse are all O(log n).
 */
public class BTree<T> {

    private class Node {
        int keyCount;
        Object[] keys;          // holds up to order-1 keys
        Object[] children;      // holds up to order child nodes
        boolean isLeaf;

        Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
            this.keys = new Object[order - 1];
            this.children = new Object[order];
            this.keyCount = 0;
        }

        /**
         * Reads a child. The array is Object[] rather than Node[] because
         * Java does not let us create an array of a generic inner class
         * directly, so we cast on the way out instead.
         */
        @SuppressWarnings("unchecked")
        Node child(int index) {
            return (Node) children[index];
        }

        void setChild(int index, Node node) {
            children[index] = node;
        }
    }

    private final int order;
    private Node root;
    private int count;
    private final Comparator<T> compare;

    private long comparisons;
    private long splits;
    private final StringBuilder splitLog = new StringBuilder();

    /**
     * @param order how many children a node may have. Must be at least 3,
     *              because an order of 2 is just a binary tree.
     */
    public BTree(int order, Comparator<T> compare) {
        if (order < 3) {
            throw new StructureException(
                    "A B-tree needs an order of at least 3, but got " + order);
        }
        if (compare == null) {
            throw new IllegalArgumentException(
                    "A B-tree needs a rule for comparing two items.");
        }
        this.order = order;
        this.compare = compare;
        this.root = new Node(true);
        this.count = 0;
    }

    public int getOrder() {
        return order;
    }

    // ---------------- searching ----------------

    @SuppressWarnings("unchecked")
    public T search(T value) {
        Node node = root;

        while (node != null) {
            int i = 0;
            // Walk along this node's keys until we find one that is not smaller.
            while (i < node.keyCount) {
                comparisons++;
                if (compare(value, (T) node.keys[i]) <= 0) {
                    break;
                }
                i++;
            }

            if (i < node.keyCount && compare(value, (T) node.keys[i]) == 0) {
                return (T) node.keys[i];
            }
            if (node.isLeaf) {
                return null;        // nowhere left to look
            }
            node = node.child(i);
        }
        return null;
    }

    public boolean contains(T value) {
        return search(value) != null;
    }

    private int compare(T a, T b) {
        return this.compare.compare(a, b);
    }

    // ---------------- inserting ----------------

    public void insert(T value) {
        if (value == null) {
            throw new StructureException("Cannot insert null into a B-tree.");
        }
        if (contains(value)) {
            throw new StructureException(
                    "This B-tree does not allow duplicates, and " + value
                            + " is already in it.");
        }

        // If the root is full we split it first. That is the only way the
        // tree gets taller, which is why every leaf stays at the same depth.
        if (root.keyCount == order - 1) {
            Node fresh = new Node(false);
            fresh.setChild(0, root);
            splitChild(fresh, 0);
            root = fresh;
        }

        insertIntoNotFull(root, value);
        count++;
    }

    @SuppressWarnings("unchecked")
    private void insertIntoNotFull(Node node, T value) {
        int i = node.keyCount - 1;

        if (node.isLeaf) {
            // Shuffle the bigger keys right to make a gap, then drop it in.
            while (i >= 0) {
                comparisons++;
                if (compare(value, (T) node.keys[i]) >= 0) {
                    break;
                }
                node.keys[i + 1] = node.keys[i];
                i--;
            }
            node.keys[i + 1] = value;
            node.keyCount++;
            return;
        }

        // Not a leaf, so work out which child to go down into.
        while (i >= 0) {
            comparisons++;
            if (compare(value, (T) node.keys[i]) >= 0) {
                break;
            }
            i--;
        }
        i++;

        // Split the child first if it is full, so we never have to come back up.
        if (node.child(i).keyCount == order - 1) {
            splitChild(node, i);
            comparisons++;
            if (compare(value, (T) node.keys[i]) > 0) {
                i++;
            }
        }
        insertIntoNotFull(node.child(i), value);
    }

    /**
     * Splits a full child in two and pushes its middle key up to the parent.
     *
     * Before:  parent [ A ]              child is full: [ p q r s t ]
     * After:   parent [ A r ]            two children:  [ p q ] and [ s t ]
     *
     * This is the operation the report needs a trace of.
     */
    private void splitChild(Node parent, int childIndex) {
        Node full = parent.child(childIndex);
        Node right = new Node(full.isLeaf);

        int middle = (order - 1) / 2;
        Object middleKey = full.keys[middle];

        splitLog.append("split: node [").append(keysOf(full))
                .append("] middle key ").append(middleKey)
                .append(" moves up to the parent\n");

        // Everything after the middle goes into the new right-hand node.
        int rightCount = order - 1 - middle - 1;
        for (int i = 0; i < rightCount; i++) {
            right.keys[i] = full.keys[middle + 1 + i];
            full.keys[middle + 1 + i] = null;
        }
        right.keyCount = rightCount;

        if (!full.isLeaf) {
            for (int i = 0; i <= rightCount; i++) {
                right.setChild(i, full.child(middle + 1 + i));
                full.setChild(middle + 1 + i, null);
            }
        }

        full.keys[middle] = null;
        full.keyCount = middle;

        // Make room in the parent for the new child and the middle key.
        for (int i = parent.keyCount; i > childIndex; i--) {
            parent.setChild(i + 1, parent.child(i));
        }
        parent.setChild(childIndex + 1, right);

        for (int i = parent.keyCount - 1; i >= childIndex; i--) {
            parent.keys[i + 1] = parent.keys[i];
        }
        parent.keys[childIndex] = middleKey;
        parent.keyCount++;

        splits++;
        splitLog.append("       left [").append(keysOf(full))
                .append("]  right [").append(keysOf(right))
                .append("]  parent now [").append(keysOf(parent)).append("]\n");
    }

    // ---------------- reading the tree ----------------

    /** Every key in sorted order. */
    public DynamicArray<T> traverse() {
        DynamicArray<T> out = new DynamicArray<>();
        collect(root, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private void collect(Node node, DynamicArray<T> out) {
        if (node == null) {
            return;
        }
        for (int i = 0; i < node.keyCount; i++) {
            if (!node.isLeaf) {
                collect(node.child(i), out);
            }
            out.add((T) node.keys[i]);
        }
        if (!node.isLeaf) {
            collect(node.child(node.keyCount), out);
        }
    }

    /** How many levels the tree has. Every leaf sits at the same level. */
    public int height() {
        int levels = 1;
        Node walker = root;
        while (!walker.isLeaf) {
            walker = walker.child(0);
            levels++;
        }
        return levels;
    }

    private String keysOf(Node node) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < node.keyCount; i++) {
            if (i > 0) {
                out.append(" ");
            }
            out.append(node.keys[i]);
        }
        return out.toString();
    }

    /** The full record of every split, for the report. */
    public String getSplitLog() {
        if (splitLog.length() == 0) {
            return "(no splits happened)";
        }
        return splitLog.toString();
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getSplits() {
        return splits;
    }

    public void clear() {
        root = new Node(true);
        count = 0;
        comparisons = 0;
        splits = 0;
        splitLog.setLength(0);
    }

    @Override
    public String toString() {
        return traverse().toString();
    }

    public String toTraceString() {
        return "order=" + order
                + "  size=" + count
                + "  height=" + height()
                + "  splits=" + splits
                + "\nroot keys=[" + keysOf(root) + "]"
                + "\nall keys=" + traverse();
    }
}
