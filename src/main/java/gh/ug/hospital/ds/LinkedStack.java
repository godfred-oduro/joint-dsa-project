package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22372639
 *
 * A stack: last thing in is the first thing out.
 *
 * Think of a pile of folders on a desk. You put new ones on top and you take
 * from the top. You cannot pull one out of the middle.
 *
 * We use this for the undo log. Every dispatch decision is pushed on, and
 * undo pops the most recent one back off (rule BR14).
 *
 * The name is LinkedStack, not Stack, because Java already has a class
 * called Stack. If someone used a wildcard import from java.util and we called ours
 * Stack too, Java would quietly use its own and break constraint C19 with
 * nothing showing on screen.
 *
 * Speed: push, pop and peek are all O(1).
 */
public class LinkedStack<T> {

    private static class Node<T> {
        T value;
        Node<T> below;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> top;
    private int count;

    /** Puts an item on top of the pile. */
    public void push(T item) {
        Node<T> box = new Node<>(item);
        box.below = top;
        top = box;
        count++;
    }

    /** Takes the top item off and hands it back. */
    public T pop() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot pop: the stack is empty. There is nothing to undo.");
        }
        T value = top.value;
        top = top.below;
        count--;
        return value;
    }

    /** Looks at the top item without taking it off. */
    public T peek() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot peek: the stack is empty.");
        }
        return top.value;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void clear() {
        top = null;
        count = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "(empty)";
        }
        StringBuilder out = new StringBuilder("top -> ");
        Node<T> walker = top;
        while (walker != null) {
            out.append(walker.value);
            if (walker.below != null) {
                out.append(" | ");
            }
            walker = walker.below;
        }
        return out.toString();
    }

    public String toTraceString() {
        return toString() + "  size=" + count;
    }
}
