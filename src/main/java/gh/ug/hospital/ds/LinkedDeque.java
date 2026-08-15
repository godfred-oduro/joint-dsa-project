package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22367946
 *
 * A deque, said "deck". Short for double-ended queue.
 *
 * It is a queue you can push onto and pull from at either end. That makes it
 * a stack and a queue at the same time, depending on which methods you use.
 *
 * In the hospital model it suits a porter's job list: routine jobs join the
 * back, but an urgent one can be pushed onto the front to be done next.
 *
 * Named LinkedDeque because java.util already has a Deque.
 *
 * Speed: every operation here is O(1).
 */
public class LinkedDeque<T> {

    private static class Node<T> {
        T value;
        Node<T> next;
        Node<T> previous;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> front;
    private Node<T> back;
    private int count;

    /** Pushes onto the front, so this item is taken next. */
    public void addFirst(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            front = box;
            back = box;
        } else {
            box.next = front;
            front.previous = box;
            front = box;
        }
        count++;
    }

    /** Joins the back, the ordinary way. */
    public void addLast(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            front = box;
            back = box;
        } else {
            box.previous = back;
            back.next = box;
            back = box;
        }
        count++;
    }

    public T removeFirst() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot remove from the front: the deque is empty.");
        }
        T value = front.value;
        front = front.next;
        if (front == null) {
            back = null;
        } else {
            front.previous = null;
        }
        count--;
        return value;
    }

    public T removeLast() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot remove from the end: the deque is empty.");
        }
        T value = back.value;
        back = back.previous;
        if (back == null) {
            front = null;
        } else {
            back.next = null;
        }
        count--;
        return value;
    }

    public T peekFirst() {
        if (isEmpty()) {
            throw new StructureException("Cannot peek: the deque is empty.");
        }
        return front.value;
    }

    public T peekLast() {
        if (isEmpty()) {
            throw new StructureException("Cannot peek: the deque is empty.");
        }
        return back.value;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public void clear() {
        front = null;
        back = null;
        count = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "(empty)";
        }
        StringBuilder out = new StringBuilder("front [ ");
        Node<T> walker = front;
        while (walker != null) {
            out.append(walker.value);
            if (walker.next != null) {
                out.append(" | ");
            }
            walker = walker.next;
        }
        out.append(" ] back");
        return out.toString();
    }

    public String toTraceString() {
        return toString() + "  size=" + count;
    }
}
