package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22367946
 *
 * Tests for LinkedDeque, which works as a stack and a queue at the same time.
 */
public class LinkedDequeTest {

    @Test
    public void ordinaryUse_behavesLikeAQueueFromBothEnds() {
        LinkedDeque<String> jobs = new LinkedDeque<>();
        jobs.addLast("routine 1");
        jobs.addLast("routine 2");

        // An urgent job jumps to the front, which a plain queue cannot do.
        jobs.addFirst("urgent");

        assertEquals("urgent", jobs.removeFirst());
        assertEquals("routine 1", jobs.removeFirst());
        assertEquals("routine 2", jobs.removeLast());
    }

    @Test
    public void ordinaryUse_peeksAtBothEnds() {
        LinkedDeque<Integer> deque = new LinkedDeque<>();
        deque.addLast(Integer.valueOf(1));
        deque.addLast(Integer.valueOf(9));

        assertEquals(Integer.valueOf(1), deque.peekFirst());
        assertEquals(Integer.valueOf(9), deque.peekLast());
        assertEquals(2, deque.size());
    }

    @Test
    public void edge_oneItemIsBothEndsAtOnce() {
        LinkedDeque<String> deque = new LinkedDeque<>();
        deque.addFirst("only");

        assertEquals("only", deque.peekFirst());
        assertEquals("only", deque.peekLast());
        assertEquals("only", deque.removeLast());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void badInput_removingFromAnEmptyDequeFails() {
        final LinkedDeque<String> empty = new LinkedDeque<>();

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.removeFirst();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.peekLast();
            }
        });
    }
}
