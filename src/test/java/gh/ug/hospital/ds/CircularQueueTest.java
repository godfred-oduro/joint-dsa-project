package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22395395
 *
 * Tests for CircularQueue. The important one is the wrap-around test, which
 * is also the evidence this structure owes to the report.
 */
public class CircularQueueTest {

    @Test
    public void ordinaryUse_firstInComesOutFirst() {
        CircularQueue<String> bays = new CircularQueue<>(4);

        bays.enqueue("ambulance 1");
        bays.enqueue("ambulance 2");

        assertEquals("ambulance 1", bays.dequeue());
        assertEquals("ambulance 2", bays.dequeue());
        assertTrue(bays.isEmpty());
    }

    @Test
    public void edge_theEndsWrapRoundToZero() {
        CircularQueue<Integer> ring = new CircularQueue<>(3);

        // Fill it, empty it, then fill it again. The markers have to come
        // back round to the start rather than run off the end.
        ring.enqueue(Integer.valueOf(1));
        ring.enqueue(Integer.valueOf(2));
        ring.enqueue(Integer.valueOf(3));
        assertTrue(ring.isFull());

        ring.dequeue();
        ring.dequeue();
        assertEquals(2, ring.getFrontIndex());

        // The back marker was at 0 after filling, so this lands at slot 0.
        ring.enqueue(Integer.valueOf(4));
        assertEquals(1, ring.getBackIndex());

        assertEquals(Integer.valueOf(3), ring.dequeue());
        assertEquals(Integer.valueOf(4), ring.dequeue());
        assertTrue(ring.isEmpty());
    }

    @Test
    public void edge_fullAndEmptyAreBothReported() {
        CircularQueue<String> queue = new CircularQueue<>(2);

        assertTrue(queue.isEmpty());
        assertFalse(queue.isFull());

        queue.enqueue("a");
        queue.enqueue("b");

        assertTrue(queue.isFull());
        assertFalse(queue.isEmpty());
        assertEquals(2, queue.capacity());
    }

    @Test
    public void badInput_addingToAFullQueueFails() {
        final CircularQueue<String> full = new CircularQueue<>(1);
        full.enqueue("only");

        // A circular queue has a fixed size, unlike the linked one. Filling
        // it up is a real thing that happens, so it has to fail clearly.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                full.enqueue("no room");
            }
        });

        final CircularQueue<String> empty = new CircularQueue<>(3);
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.dequeue();
            }
        });
    }
}
