package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22015166
 *
 * Tests for LinkedQueue, the structure BFS uses to decide where to look next.
 */
public class LinkedQueueTest {

    @Test
    public void ordinaryUse_nobodyJumpsTheLine() {
        LinkedQueue<String> waiting = new LinkedQueue<>();
        waiting.enqueue("first");
        waiting.enqueue("second");
        waiting.enqueue("third");

        assertEquals("first", waiting.dequeue());
        assertEquals("second", waiting.dequeue());
        assertEquals("third", waiting.dequeue());
    }

    @Test
    public void ordinaryUse_peekDoesNotServeAnyone() {
        LinkedQueue<String> queue = new LinkedQueue<>();
        queue.enqueue("waiting");

        assertEquals("waiting", queue.peek());
        assertEquals(1, queue.size());
    }

    @Test
    public void edge_emptyingThenRefillingWorks() {
        LinkedQueue<Integer> queue = new LinkedQueue<>();
        queue.enqueue(Integer.valueOf(1));
        queue.dequeue();
        assertTrue(queue.isEmpty());

        queue.enqueue(Integer.valueOf(2));
        assertEquals(Integer.valueOf(2), queue.peek());
        assertEquals(1, queue.size());
    }

    @Test
    public void badInput_servingAnEmptyQueueFails() {
        final LinkedQueue<String> empty = new LinkedQueue<>();

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.dequeue();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.peek();
            }
        });
    }
}
