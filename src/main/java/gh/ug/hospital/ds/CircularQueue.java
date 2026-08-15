package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22395395
 *
 * A queue built on a fixed array, where the ends wrap round.
 *
 * Picture the array bent into a ring. The front marker and the back marker
 * both move clockwise. When a marker walks off the end of the array it comes
 * back to position zero. That is why there is a % (remainder) in the code.
 *
 * Why bother, when LinkedQueue already works? Because this one never creates
 * new objects while it runs. It is the right choice when you know the most
 * items you will ever hold, such as a fixed number of ambulance bays.
 *
 * The catch is that it can be full. LinkedQueue never can.
 *
 * Speed: enqueue, dequeue and peek are all O(1).
 */
public class CircularQueue<T> {

    private final Object[] slots;
    private int front;      // where the next dequeue will read from
    private int back;       // where the next enqueue will write to
    private int count;

    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new StructureException(
                    "A circular queue needs room for at least 1 item, but got " + capacity);
        }
        this.slots = new Object[capacity];
        this.front = 0;
        this.back = 0;
        this.count = 0;
    }

    /** Joins the back of the queue. Fails if there is no room. */
    public void enqueue(T item) {
        if (isFull()) {
            throw new StructureException(
                    "Cannot enqueue: the circular queue is full at "
                            + slots.length + " items.");
        }
        slots[back] = item;
        // Step forward, and wrap round to 0 if we fall off the end.
        back = (back + 1) % slots.length;
        count++;
    }

    /** Serves the front item and removes it. */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot dequeue: the circular queue is empty.");
        }
        T value = (T) slots[front];
        slots[front] = null;              // let Java tidy the old object away
        front = (front + 1) % slots.length;
        count--;
        return value;
    }

    /** Looks at the front item without removing it. */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new StructureException(
                    "Cannot peek: the circular queue is empty.");
        }
        return (T) slots[front];
    }

    public boolean isFull() {
        return count == slots.length;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }

    public int capacity() {
        return slots.length;
    }

    public int getFrontIndex() {
        return front;
    }

    public int getBackIndex() {
        return back;
    }

    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
        front = 0;
        back = 0;
        count = 0;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "(empty)";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int slot = (front + i) % slots.length;
            out.append(slots[slot]);
            if (i < count - 1) {
                out.append(" | ");
            }
        }
        return out.toString();
    }

    /**
     * Shows the raw array with the two markers, which is the evidence this
     * structure owes: you can watch front and back wrap round to zero.
     */
    public String toTraceString() {
        StringBuilder out = new StringBuilder("slots=[");
        for (int i = 0; i < slots.length; i++) {
            if (i > 0) {
                out.append(", ");
            }
            if (slots[i] == null) {
                out.append("_");
            } else {
                out.append(slots[i]);
            }
        }
        out.append("]  front=").append(front)
           .append("  back=").append(back)
           .append("  size=").append(count).append("/").append(slots.length);
        return out.toString();
    }
}
