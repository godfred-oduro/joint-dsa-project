package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

import java.util.Comparator;

/**
 * Owner: 22040255
 *
 * A binary search tree that keeps itself balanced.
 *
 * The plain BinarySearchTree has one bad habit: feed it sorted data and it
 * turns into a long thin chain, so searching costs O(n) instead of O(log n).
 * This class fixes that by colouring every node red or black and obeying
 * five rules:
 *
 *   1. Every node is red or black.
 *   2. The root is black.
 *   3. Every leaf (the empty NIL slots) is black.
 *   4. A red node cannot have a red child.
 *   5. Every path from a node down to a leaf passes the same number of
 *      black nodes.
 *
 * Rules 4 and 5 together mean the longest path can never be more than twice
 * the shortest, so the tree stays roughly balanced. When an insert breaks a
 * rule we fix it by recolouring nodes and by rotating, which means pivoting
 * a parent and child around each other without changing the sorted order.
 *
 * Experiment E4 puts this side by side with the plain tree, and the
 * difference in height is the point of the whole exercise.
 *
 * We use one shared black NIL node instead of null for the empty slots.
 * That removes a great many null checks and is how the textbook does it.
 *
 * Speed: insert, search and delete are all O(log n), guaranteed.
 */
public class RedBlackTree<T> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        T value;
        Node left;
        Node right;
        Node parent;
        boolean colour;

        Node(T value, boolean colour) {
            this.value = value;
            this.colour = colour;
        }
    }

    /** The shared empty leaf. Always black, never holds a value. */
    private final Node nil;
    private Node root;
    private int count;

    private final Comparator<T> order;

    private long comparisons;
    private long rotations;
    private long recolours;

    /** Written into as rotations happen, so the report can show before and after. */
    private final StringBuilder rotationLog = new StringBuilder();

    /**
     * Whether logRotation() records anything. On by default, because the
     * demo menu and the evidence menu both rely on getRotationLog() and
     * only ever insert a handful of items, so the cost is nothing.
     *
     * Off for the timing experiments. Building the log means walking the
     * whole tree and writing out its shape as a string on every rotation,
     * which is O(n) work paid on every one of the O(n) rotations a big
     * insert can cause. Left on, a ten-thousand-item insert into E4 does
     * roughly n times that O(n) walk, which is why RedBlackTree-sorted was
     * measuring far slower than the plain BST it is supposed to be beating.
     * The tree itself is not slow; the logging was.
     */
    private boolean loggingEnabled = true;

    public RedBlackTree(Comparator<T> order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "A red-black tree needs a rule for comparing two items.");
        }
        this.order = order;
        this.nil = new Node(null, BLACK);
        this.root = nil;
        this.count = 0;
    }

    // ---------------- inserting ----------------

    public void insert(T value) {
        if (value == null) {
            throw new StructureException("Cannot insert null into a red-black tree.");
        }

        Node fresh = new Node(value, RED);   // new nodes always start red
        fresh.left = nil;
        fresh.right = nil;

        Node parent = nil;
        Node walker = root;

        // Ordinary search-tree descent to find where it belongs.
        while (walker != nil) {
            parent = walker;
            comparisons++;
            int side = order.compare(value, walker.value);
            if (side < 0) {
                walker = walker.left;
            } else if (side > 0) {
                walker = walker.right;
            } else {
                throw new StructureException(
                        "This tree does not allow duplicates, and " + value
                                + " is already in it.");
            }
        }

        fresh.parent = parent;
        if (parent == nil) {
            root = fresh;
        } else if (order.compare(value, parent.value) < 0) {
            parent.left = fresh;
        } else {
            parent.right = fresh;
        }

        count++;
        fixAfterInsert(fresh);
    }

    /**
     * The new node is red, so the only rule it can break is rule 4: no red
     * node may have a red child. We walk up fixing that.
     */
    private void fixAfterInsert(Node node) {
        while (node.parent.colour == RED) {
            Node parent = node.parent;
            Node grandparent = parent.parent;

            if (parent == grandparent.left) {
                Node uncle = grandparent.right;

                if (uncle.colour == RED) {
                    // Case 1: the uncle is red too. Recolour and move up.
                    parent.colour = BLACK;
                    uncle.colour = BLACK;
                    grandparent.colour = RED;
                    recolours += 3;
                    node = grandparent;
                } else {
                    if (node == parent.right) {
                        // Case 2: node is on the inside. Rotate to make it outside.
                        node = parent;
                        rotateLeft(node);
                        parent = node.parent;
                        grandparent = parent.parent;
                    }
                    // Case 3: node is on the outside. Recolour and rotate once.
                    parent.colour = BLACK;
                    grandparent.colour = RED;
                    recolours += 2;
                    rotateRight(grandparent);
                }
            } else {
                // Exactly the same, with left and right swapped over.
                Node uncle = grandparent.left;

                if (uncle.colour == RED) {
                    parent.colour = BLACK;
                    uncle.colour = BLACK;
                    grandparent.colour = RED;
                    recolours += 3;
                    node = grandparent;
                } else {
                    if (node == parent.left) {
                        node = parent;
                        rotateRight(node);
                        parent = node.parent;
                        grandparent = parent.parent;
                    }
                    parent.colour = BLACK;
                    grandparent.colour = RED;
                    recolours += 2;
                    rotateLeft(grandparent);
                }
            }
        }
        root.colour = BLACK;    // rule 2, always safe to enforce
    }

    // ---------------- rotating ----------------

    /**
     * Pivots node and its right child around each other.
     *
     *     x                y
     *      \              /
     *       y    ==>     x
     *      /              \
     *     b                b
     *
     * The sorted order is unchanged. Only the shape moves.
     */
    private void rotateLeft(Node x) {
        logRotation("left", x);

        Node y = x.right;
        x.right = y.left;
        if (y.left != nil) {
            y.left.parent = x;
        }
        y.parent = x.parent;

        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
        rotations++;
    }

    /** The mirror image of rotateLeft. */
    private void rotateRight(Node x) {
        logRotation("right", x);

        Node y = x.left;
        x.left = y.right;
        if (y.right != nil) {
            y.right.parent = x;
        }
        y.parent = x.parent;

        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }

        y.right = x;
        x.parent = y;
        rotations++;
    }

    private void logRotation(String direction, Node pivot) {
        if (!loggingEnabled) {
            return;
        }
        rotationLog.append("rotate ").append(direction)
                .append(" around ").append(pivot.value)
                .append("   before: ").append(shape(root))
                .append("\n");
    }

    /**
     * Turns the rotation log on or off. Call this with false before building
     * a large tree purely for timing, so the O(n)-per-rotation logging does
     * not swamp the measurement. See the note on loggingEnabled above.
     */
    public void setLoggingEnabled(boolean enabled) {
        this.loggingEnabled = enabled;
    }

    // ---------------- searching ----------------

    public T search(T value) {
        Node walker = root;
        while (walker != nil) {
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

    private Node findNode(T value) {
        Node walker = root;
        while (walker != nil) {
            int side = order.compare(value, walker.value);
            if (side == 0) {
                return walker;
            }
            if (side < 0) {
                walker = walker.left;
            } else {
                walker = walker.right;
            }
        }
        return nil;
    }

    // ---------------- deleting ----------------

    public void delete(T value) {
        Node target = findNode(value);
        if (target == nil) {
            throw new StructureException(value + " is not in the tree.");
        }

        Node removedOrMoved = target;
        boolean removedColour = removedOrMoved.colour;
        Node replacement;

        if (target.left == nil) {
            replacement = target.right;
            transplant(target, target.right);
        } else if (target.right == nil) {
            replacement = target.left;
            transplant(target, target.left);
        } else {
            // Two children. Use the smallest value on the right, as usual.
            removedOrMoved = smallestBelow(target.right);
            removedColour = removedOrMoved.colour;
            replacement = removedOrMoved.right;

            if (removedOrMoved.parent == target) {
                replacement.parent = removedOrMoved;
            } else {
                transplant(removedOrMoved, removedOrMoved.right);
                removedOrMoved.right = target.right;
                removedOrMoved.right.parent = removedOrMoved;
            }
            transplant(target, removedOrMoved);
            removedOrMoved.left = target.left;
            removedOrMoved.left.parent = removedOrMoved;
            removedOrMoved.colour = target.colour;
        }

        count--;

        // Removing a black node breaks rule 5, so we have to repair.
        if (removedColour == BLACK) {
            fixAfterDelete(replacement);
        }
    }

    /** Puts one subtree where another one was. */
    private void transplant(Node oldNode, Node newNode) {
        if (oldNode.parent == nil) {
            root = newNode;
        } else if (oldNode == oldNode.parent.left) {
            oldNode.parent.left = newNode;
        } else {
            oldNode.parent.right = newNode;
        }
        newNode.parent = oldNode.parent;
    }

    private Node smallestBelow(Node node) {
        while (node.left != nil) {
            node = node.left;
        }
        return node;
    }

    /**
     * Repairs rule 5 after a black node was removed. The node we are looking
     * at is carrying one black too few, and we push that shortage upward
     * until it can be absorbed.
     */
    private void fixAfterDelete(Node node) {
        while (node != root && node.colour == BLACK) {
            if (node == node.parent.left) {
                Node sibling = node.parent.right;

                if (sibling.colour == RED) {
                    sibling.colour = BLACK;
                    node.parent.colour = RED;
                    recolours += 2;
                    rotateLeft(node.parent);
                    sibling = node.parent.right;
                }

                if (sibling.left.colour == BLACK && sibling.right.colour == BLACK) {
                    sibling.colour = RED;
                    recolours++;
                    node = node.parent;
                } else {
                    if (sibling.right.colour == BLACK) {
                        sibling.left.colour = BLACK;
                        sibling.colour = RED;
                        recolours += 2;
                        rotateRight(sibling);
                        sibling = node.parent.right;
                    }
                    sibling.colour = node.parent.colour;
                    node.parent.colour = BLACK;
                    sibling.right.colour = BLACK;
                    recolours += 3;
                    rotateLeft(node.parent);
                    node = root;
                }
            } else {
                // Mirror image.
                Node sibling = node.parent.left;

                if (sibling.colour == RED) {
                    sibling.colour = BLACK;
                    node.parent.colour = RED;
                    recolours += 2;
                    rotateRight(node.parent);
                    sibling = node.parent.left;
                }

                if (sibling.right.colour == BLACK && sibling.left.colour == BLACK) {
                    sibling.colour = RED;
                    recolours++;
                    node = node.parent;
                } else {
                    if (sibling.left.colour == BLACK) {
                        sibling.right.colour = BLACK;
                        sibling.colour = RED;
                        recolours += 2;
                        rotateLeft(sibling);
                        sibling = node.parent.left;
                    }
                    sibling.colour = node.parent.colour;
                    node.parent.colour = BLACK;
                    sibling.left.colour = BLACK;
                    recolours += 3;
                    rotateRight(node.parent);
                    node = root;
                }
            }
        }
        node.colour = BLACK;
    }

    // ---------------- reading the tree ----------------

    public DynamicArray<T> inorder() {
        DynamicArray<T> out = new DynamicArray<>();
        collectInorder(root, out);
        return out;
    }

    private void collectInorder(Node node, DynamicArray<T> out) {
        if (node == nil) {
            return;
        }
        collectInorder(node.left, out);
        out.add(node.value);
        collectInorder(node.right, out);
    }

    /** Total height, counting every node. */
    public int height() {
        return heightBelow(root);
    }

    private int heightBelow(Node node) {
        if (node == nil) {
            return 0;
        }
        int left = heightBelow(node.left);
        int right = heightBelow(node.right);
        if (left > right) {
            return left + 1;
        }
        return right + 1;
    }

    /**
     * How many black nodes sit on any path from the root down to a leaf.
     * Rule 5 says this is the same whichever path you take, so if this
     * method disagrees with itself the tree is broken.
     */
    public int blackHeight() {
        int black = 0;
        Node walker = root;
        while (walker != nil) {
            if (walker.colour == BLACK) {
                black++;
            }
            walker = walker.left;
        }
        return black;
    }

    /** A quick shape sketch, used in the rotation log. */
    private String shape(Node node) {
        if (node == nil) {
            return ".";
        }
        String mark;
        if (node.colour == RED) {
            mark = "R";
        } else {
            mark = "B";
        }
        return "(" + shape(node.left) + " " + node.value + mark + " " + shape(node.right) + ")";
    }

    /** The full before-and-after record of every rotation, for the report. */
    public String getRotationLog() {
        if (rotationLog.length() == 0) {
            return "(no rotations happened)";
        }
        return rotationLog.toString();
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

    public long getRotations() {
        return rotations;
    }

    public long getRecolours() {
        return recolours;
    }

    public void clear() {
        root = nil;
        count = 0;
        comparisons = 0;
        rotations = 0;
        recolours = 0;
        rotationLog.setLength(0);
    }

    @Override
    public String toString() {
        return inorder().toString();
    }

    public String toTraceString() {
        return "shape=" + shape(root)
                + "\ninorder=" + inorder()
                + "\nsize=" + count
                + "  height=" + height()
                + "  black height=" + blackHeight()
                + "  rotations=" + rotations
                + "  recolours=" + recolours;
    }
}
