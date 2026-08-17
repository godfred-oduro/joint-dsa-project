package gh.ug.hospital.ds;


import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22399010
 *
 * Tests for HashBackedSet, which BFS and DFS use to remember where they have been.
 */
public class HashBackedSetTest {

    @Test
    public void ordinaryUse_nothingAppearsTwice() {
        HashBackedSet<String> visited = new HashBackedSet<>(101);

        assertTrue(visited.add("L001"));
        assertFalse(visited.add("L001"));   // already there, so nothing happens
        assertEquals(1, visited.size());
        assertTrue(visited.contains("L001"));
    }

    @Test
    public void ordinaryUse_removesWhatIsThere() {
        HashBackedSet<String> set = new HashBackedSet<>(31);
        set.add("a");

        assertTrue(set.remove("a"));
        assertFalse(set.remove("a"));       // gone already
        assertTrue(set.isEmpty());
    }

    @Test
    public void ordinaryUse_joinsAndComparesSets() {
        HashBackedSet<String> morning = new HashBackedSet<>(31);
        morning.add("porter1");
        morning.add("porter2");

        HashBackedSet<String> afternoon = new HashBackedSet<>(31);
        afternoon.add("porter2");
        afternoon.add("porter3");

        assertEquals(3, morning.union(afternoon).size());
        assertEquals(1, morning.intersection(afternoon).size());
        assertEquals(1, morning.difference(afternoon).size());
        assertTrue(morning.intersection(afternoon).contains("porter2"));
    }

    @Test
    public void badInput_addingNullIsRefused() {
        final HashBackedSet<String> set = new HashBackedSet<>(11);

        // The set stores its items as keys in a map, and a key cannot be null.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                set.add(null);
            }
        });
    }
}
