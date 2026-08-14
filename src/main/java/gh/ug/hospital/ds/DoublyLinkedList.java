package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22372680
 *
 * A chain of boxes where each box points both forwards and backwards.
 *
 * The singly linked list can only be walked one way. Here each box also
 * remembers the box before it, so we can walk from either end. That costs a
 * little extra memory but makes removeLast instant instead of slow.
 *
 * Speed:
 *   addFirst, addLast, removeFirst, removeLast   O(1)
 *   get and contains                             O(n)
 */
public class DoublyLinkedList<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node<T> previous;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> first;
    private Node<T> last;
    private int count;

    public void addFirst(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            first = box;
            last = box;
        } else {
            box.next = first;
            first.previous = box;
            first = box;
        }
        count++;
    }

    public void addLast(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            first = box;
            last = box;
        } else {
            box.previous = last;
            last.next = box;
            last = box;
        }
        count++;
    }

    public T removeFirst() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot remove from the front: the list is empty.");
        }
        T value = first.value;
        first = first.next;
        if (first == null) {
            last = null;
        } else {
            first.previous = null;
        }
        count--;
        return value;
    }

    public T removeLast() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot remove from the end: the list is empty.");
        }
        T value = last.value;
        last = last.previous;
        if (last == null) {
            first = null;
        } else {
            last.next = null;
        }
        count--;
        return value;
    }

    public T get(int index) {
        if (index < 0 || index >= count) {
            throw new StructureException(
                    "Position " + index + " does not exist. The list holds "
                            + count + " items.");
        }
        // Start from whichever end is closer. Small saving, easy to explain.
        if (index < count / 2) {
            Node<T> walker = first;
            for (int i = 0; i < index; i++) {
                walker = walker.next;
            }
            return walker.value;
        }
        Node<T> walker = last;
        for (int i = count - 1; i > index; i--) {
            walker = walker.previous;
        }
        return walker.value;
    }

    public boolean contains(T item) {
        Node<T> walker = first;
        while (walker != null) {
            if (item == null) {
                if (walker.value == null) {
                    return true;
                }
            } else if (item.equals(walker.value)) {
                return true;
            }
            walker = walker.next;
        }
        return false;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void clear() {
        first = null;
        last = null;
        count = 0;
    }

    /** Walks front to back. */
    public class ForwardWalker {
        private Node<T> position = first;

        public boolean hasNext() {
            return position != null;
        }

        public T next() {
            if (position == null) {
                throw new StructureException(
                        "The walker has passed the end of the list.");
            }
            T value = position.value;
            position = position.next;
            return value;
        }
    }

    /** Walks back to front. This is what the second pointer buys us. */
    public class BackwardWalker {
        private Node<T> position = last;

        public boolean hasNext() {
            return position != null;
        }

        public T next() {
            if (position == null) {
                throw new StructureException(
                        "The walker has passed the start of the list.");
            }
            T value = position.value;
            position = position.previous;
            return value;
        }
    }

    public ForwardWalker forwardWalker() {
        return new ForwardWalker();
    }

    public BackwardWalker backwardWalker() {
        return new BackwardWalker();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        Node<T> walker = first;
        while (walker != null) {
            out.append(walker.value);
            if (walker.next != null) {
                out.append(" <-> ");
            }
            walker = walker.next;
        }
        if (out.length() == 0) {
            return "(empty)";
        }
        return out.toString();
    }

    /** Prints the list both ways, which is the evidence this structure owes. */
    public String toTraceString() {
        StringBuilder backwards = new StringBuilder();
        BackwardWalker back = backwardWalker();
        while (back.hasNext()) {
            backwards.append(back.next());
            if (back.hasNext()) {
                backwards.append(" <- ");
            }
        }
        return "forward:  " + toString() + "\n"
                + "backward: " + backwards + "\n"
                + "size=" + count;
    }
}
