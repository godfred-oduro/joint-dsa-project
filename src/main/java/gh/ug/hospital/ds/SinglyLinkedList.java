package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22392509
 *
 * A chain of boxes, each one pointing at the next.
 *
 * Unlike an array, the boxes are not side by side in memory. Each box holds
 * an item and the address of the next box. To reach box number five you have
 * to walk through the first four. That is why get() is slow here but adding
 * at the front is instant.
 *
 * Speed:
 *   addFirst, removeFirst   O(1)
 *   addLast                 O(1), because we keep a pointer to the last box
 *   removeLast              O(n), we must find the box before the last one
 *   get and contains        O(n)
 */
public class SinglyLinkedList<T> {

    /** One box in the chain. */
    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
            this.next = null;
        }
    }

    private Node<T> first;
    private Node<T> last;
    private int count;

    public SinglyLinkedList() {
        first = null;
        last = null;
        count = 0;
    }

    /** Adds to the front of the chain. */
    public void addFirst(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            first = box;
            last = box;
        } else {
            box.next = first;
            first = box;
        }
        count++;
    }

    /** Adds to the end of the chain. */
    public void addLast(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            first = box;
            last = box;
        } else {
            last.next = box;
            last = box;
        }
        count++;
    }

    /** Takes the first item out and hands it back. */
    public T removeFirst() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot remove from the front: the list is empty.");
        }
        T value = first.value;
        first = first.next;
        if (first == null) {
            last = null;      // the list is now empty
        }
        count--;
        return value;
    }

    /** Takes the last item out and hands it back. */
    public T removeLast() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot remove from the end: the list is empty.");
        }
        if (count == 1) {
            T only = first.value;
            first = null;
            last = null;
            count = 0;
            return only;
        }

        // Walk to the box just before the last one.
        Node<T> walker = first;
        while (walker.next != last) {
            walker = walker.next;
        }
        T value = last.value;
        walker.next = null;
        last = walker;
        count--;
        return value;
    }

    /** Reads the item at a position, counting from zero. */
    public T get(int index) {
        if (index < 0 || index >= count) {
            throw new StructureException(
                    "Position " + index + " does not exist. The list holds "
                            + count + " items.");
        }
        Node<T> walker = first;
        for (int i = 0; i < index; i++) {
            walker = walker.next;
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

    /**
     * A walker that visits every item once, front to back.
     *
     * We wrote our own instead of using Java's Iterator, because the whole
     * point of the project is to build these things ourselves.
     */
    public class ListWalker {
        private Node<T> position = first;

        /** True if there is still something left to visit. */
        public boolean hasNext() {
            return position != null;
        }

        /** Hands back the next item and steps forward. */
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

    public ListWalker walker() {
        return new ListWalker();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        Node<T> walker = first;
        while (walker != null) {
            out.append(walker.value);
            if (walker.next != null) {
                out.append(" -> ");
            }
            walker = walker.next;
        }
        if (out.length() == 0) {
            return "(empty)";
        }
        return out.toString();
    }

    public String toTraceString() {
        return toString() + "  size=" + count;
    }
}
