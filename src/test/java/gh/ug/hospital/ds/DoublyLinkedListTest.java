package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22372680
 *
 * Tests for DoublyLinkedList. The backward walk is what the second pointer buys us.
 */
public class DoublyLinkedListTest {

    @Test
    public void ordinaryUse_holdsItemsInOrder() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("a");
        list.addLast("b");
        list.addFirst("start");

        assertEquals(3, list.size());
        assertEquals("start", list.get(0));
        assertEquals("b", list.get(2));
    }

    @Test
    public void ordinaryUse_walksBackwardsAsWellAsForwards() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        list.addLast(Integer.valueOf(1));
        list.addLast(Integer.valueOf(2));
        list.addLast(Integer.valueOf(3));

        DoublyLinkedList<Integer>.BackwardWalker back = list.backwardWalker();
        assertEquals(Integer.valueOf(3), back.next());
        assertEquals(Integer.valueOf(2), back.next());
        assertEquals(Integer.valueOf(1), back.next());
        assertFalse(back.hasNext());
    }

    @Test
    public void edge_removingFromTheEndIsInstant() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("first");
        list.addLast("last");

        assertEquals("last", list.removeLast());
        assertEquals("first", list.removeLast());
        assertTrue(list.isEmpty());
    }

    @Test
    public void badInput_removingFromAnEmptyListFails() {
        final DoublyLinkedList<String> empty = new DoublyLinkedList<>();

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.removeLast();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.removeFirst();
            }
        });
    }
}
