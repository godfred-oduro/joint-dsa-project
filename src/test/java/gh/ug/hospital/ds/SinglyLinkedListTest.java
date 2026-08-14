package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22392509
 *
 * Tests for SinglyLinkedList, including the walker that visits every item.
 */
public class SinglyLinkedListTest {

    @Test
    public void ordinaryUse_addsAtBothEnds() {
        SinglyLinkedList<String> jobs = new SinglyLinkedList<>();
        jobs.addLast("Q002");
        jobs.addLast("Q003");
        jobs.addFirst("Q001");

        assertEquals(3, jobs.size());
        assertEquals("Q001", jobs.get(0));
        assertEquals("Q003", jobs.get(2));
        assertTrue(jobs.contains("Q002"));
    }

    @Test
    public void ordinaryUse_theWalkerVisitsEveryItemOnce() {
        SinglyLinkedList<Integer> list = new SinglyLinkedList<>();
        list.addLast(Integer.valueOf(1));
        list.addLast(Integer.valueOf(2));
        list.addLast(Integer.valueOf(3));

        SinglyLinkedList<Integer>.ListWalker walker = list.walker();
        int total = 0;
        int visited = 0;
        while (walker.hasNext()) {
            total += walker.next().intValue();
            visited++;
        }

        assertEquals(3, visited);
        assertEquals(6, total);
    }

    @Test
    public void edge_oneItemListStillWorksAtBothEnds() {
        SinglyLinkedList<String> list = new SinglyLinkedList<>();
        list.addFirst("only");

        assertEquals("only", list.removeLast());
        assertTrue(list.isEmpty());
    }

    @Test
    public void badInput_removingFromAnEmptyListFails() {
        final SinglyLinkedList<String> empty = new SinglyLinkedList<>();

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.removeFirst();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.get(0);
            }
        });
    }
}
