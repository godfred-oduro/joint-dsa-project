package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22015166
 *
 * A queue: first thing in is the first thing out.
 *
 * This is an ordinary line of people. New arrivals join the back, and the
 * person at the front is served next. Nobody jumps the line.
 *
 * We use this for jobs where urgency does not apply, and BFS uses it to
 * decide which location to visit next.
 *
 * Named LinkedQueue rather than Queue for the same reason as LinkedStack:
 * java.util already has a Queue.
 *
 * Speed: enqueue, dequeue and peek are all O(1).
 */
public class LinkedQueue<T> {

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> front;
    private Node<T> back;
    private int count;

    /** Joins the back of the queue. */
    public void enqueue(T item) {
        Node<T> box = new Node<>(item);
        if (isEmpty()) {
            front = box;
            back = box;
        } else {
            back.next = box;
            back = box;
        }
        count++;
    }

    /** Serves the person at the front and removes them. */
    public T dequeue() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot dequeue: the queue is empty. Nobody is waiting.");
        }
        T value = front.value;
        front = front.next;
        if (front == null) {
            back = null;
        }
        count--;
        return value;
    }

    /** Looks at the front without serving them. */
    public T peek() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot peek: the queue is empty.");
        }
        return front.value;
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
        StringBuilder out = new StringBuilder("front -> ");
        Node<T> walker = front;
        while (walker != null) {
            out.append(walker.value);
            if (walker.next != null) {
                out.append(" | ");
            }
            walker = walker.next;
        }
        out.append(" <- back");
        return out.toString();
    }

    public String toTraceString() {
        return toString() + "  size=" + count;
    }
}
