package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22372639
 *
 * Tests for LinkedStack, the structure behind the undo feature.
 */
public class LinkedStackTest {

    @Test
    public void ordinaryUse_lastInComesOutFirst() {
        LinkedStack<String> decisions = new LinkedStack<>();

        decisions.push("assigned Q001");
        decisions.push("assigned Q002");
        decisions.push("assigned Q003");

        // Undo should reverse the most recent decision first.
        assertEquals("assigned Q003", decisions.pop());
        assertEquals("assigned Q002", decisions.pop());
        assertEquals("assigned Q001", decisions.pop());
        assertTrue(decisions.isEmpty());
    }

    @Test
    public void ordinaryUse_peekLooksWithoutRemoving() {
        LinkedStack<Integer> stack = new LinkedStack<>();
        stack.push(Integer.valueOf(7));

        assertEquals(Integer.valueOf(7), stack.peek());
        assertEquals(1, stack.size());     // still there
    }

    @Test
    public void edge_oneItemPushedAndPopped() {
        LinkedStack<String> stack = new LinkedStack<>();
        stack.push("only");

        assertEquals(1, stack.size());
        assertEquals("only", stack.pop());
        assertEquals(0, stack.size());
        assertTrue(stack.isEmpty());
    }

    @Test
    public void badInput_poppingAnEmptyStackFails() {
        final LinkedStack<String> empty = new LinkedStack<>();

        // This is what happens when someone asks to undo with nothing done yet.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.pop();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                empty.peek();
            }
        });
    }
}
