package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

import java.util.Comparator;

/**
 * Owner: 22305896
 *
 * A binary search tree. Keeps items sorted without ever running a sort.
 *
 * The rule is simple: everything smaller than a node goes in its left
 * branch, everything bigger goes right. To find something you start at the
 * top and keep choosing left or right, so you throw away half the tree at
 * every step, as long as the tree is balanced.
 *
 * The catch is that nothing here keeps it balanced. If you insert already
 * sorted data, every item goes right and the tree becomes a long thin chain.
 * Searching then costs O(n), no better than a list. That is exactly what
 * experiment E4 measures, and it is why the red-black tree exists.
 *
 * Duplicates are refused. Every location code and request code in this
 * project is unique, so a duplicate means the data is wrong.
 *
 * Speed:
 *   insert, search, delete   O(log n) when balanced, O(n) at worst
 *   inorder                  O(n)
 */
public class BinarySearchTree<T> {

    private static class Node<T> {
        T value;
        Node<T> left;
        Node<T> right;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> root;
    private int count;
    private final Comparator<T> order;

    /** Counts comparisons so the search-path trace can report them. */
    private long comparisons;

    public BinarySearchTree(Comparator<T> order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "A search tree needs a rule for comparing two items.");
        }
        this.order = order;
    }

    public void insert(T value) {
        if (value == null) {
            throw new StructureException("Cannot insert null into a search tree.");
        }
        root = insertBelow(root, value);
        count++;
    }

    private Node<T> insertBelow(Node<T> node, T value) {
        if (node == null) {
            return new Node<>(value);
        }
        comparisons++;
        int side = order.compare(value, node.value);

        if (side < 0) {
            node.left = insertBelow(node.left, value);
        } else if (side > 0) {
            node.right = insertBelow(node.right, value);
        } else {
            throw new StructureException(
                    "This tree does not allow duplicates, and " + value
                            + " is already in it.");
        }
        return node;
    }

    /** Finds the item, or hands back null if it is not there. */
    public T search(T value) {
        Node<T> walker = root;
        while (walker != null) {
            comparisons++;
            int side = order.compare(value, walker.value);
            if (side == 0) {
                return walker.value;
            }
            if (side < 0) {
                walker = walker.left;
            } else {
                walker = walker.right;
            }
        }
        return null;
    }

    public boolean contains(T value) {
        return search(value) != null;
    }

    /**
     * The route taken while searching, written out. This is the evidence
     * this structure owes: it shows how few nodes a balanced tree touches,
     * and how many a lopsided one touches.
     */
    public String searchPath(T value) {
        StringBuilder path = new StringBuilder();
        Node<T> walker = root;
        int steps = 0;

        while (walker != null) {
            if (steps > 0) {
                path.append(" -> ");
            }
            path.append(walker.value);
            steps++;

            int side = order.compare(value, walker.value);
            if (side == 0) {
                return path + "   (found after " + steps + " steps)";
            }
            if (side < 0) {
                walker = walker.left;
            } else {
                walker = walker.right;
            }
        }
        return path + "   (not found, gave up after " + steps + " steps)";
    }

    /** Removes an item. */
    public void delete(T value) {
        if (root == null) {
            throw new StructureException("Cannot delete: the tree is empty.");
        }
        int before = count;
        root = deleteBelow(root, value);
        if (count == before) {
            // deleteBelow only lowers the count when it actually removed something
            throw new gh.ug.hospital.exception.StructureException(
                    value + " is not in the tree, so it cannot be deleted.");
        }
    }

    private Node<T> deleteBelow(Node<T> node, T value) {
        if (node == null) {
            return null;
        }
        comparisons++;
        int side = order.compare(value, node.value);

        if (side < 0) {
            node.left = deleteBelow(node.left, value);
            return node;
        }
        if (side > 0) {
            node.right = deleteBelow(node.right, value);
            return node;
        }

        // Found it. Three cases.
        count--;

        if (node.left == null) {
            return node.right;          // no left child, so the right one takes over
        }
        if (node.right == null) {
            return node.left;           // no right child
        }

        // Two children. Replace this node's value with the smallest value in
        // its right branch, then delete that smallest node instead.
        Node<T> smallest = node.right;
        while (smallest.left != null) {
            smallest = smallest.left;
        }
        node.value = smallest.value;
        count++;                        // undo the decrement, the real removal is below
        node.right = deleteBelow(node.right, smallest.value);
        return node;
    }

    /**
     * Every item in sorted order.
     *
     * Walking left, then the node, then right always comes out sorted.
     * That is the whole point of the tree.
     */
    public DynamicArray<T> inorder() {
        DynamicArray<T> out = new DynamicArray<>();
        collectInorder(root, out);
        return out;
    }

    private void collectInorder(Node<T> node, DynamicArray<T> out) {
        if (node == null) {
            return;
        }
        collectInorder(node.left, out);
        out.add(node.value);
        collectInorder(node.right, out);
    }

    /** How many levels deep the tree goes. An empty tree has height 0. */
    public int height() {
        return heightBelow(root);
    }

    private int heightBelow(Node<T> node) {
        if (node == null) {
            return 0;
        }
        int left = heightBelow(node.left);
        int right = heightBelow(node.right);
        if (left > right) {
            return left + 1;
        }
        return right + 1;
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

    public void resetComparisons() {
        comparisons = 0;
    }

    public void clear() {
        root = null;
        count = 0;
        comparisons = 0;
    }

    @Override
    public String toString() {
        return inorder().toString();
    }

    public String toTraceString() {
        return "inorder=" + inorder()
                + "  size=" + count
                + "  height=" + height()
                + "  comparisons=" + comparisons;
    }
}
