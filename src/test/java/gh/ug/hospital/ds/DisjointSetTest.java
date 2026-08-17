package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 11102882
 *
 * Tests for DisjointSet, the structure Kruskal uses to spot loops.
 */
public class DisjointSetTest {

    @Test
    public void ordinaryUse_everythingStartsInItsOwnGroup() {
        DisjointSet<String> groups = new DisjointSet<>();
        groups.makeSet("L001");
        groups.makeSet("L002");
        groups.makeSet("L003");

        assertEquals(3, groups.componentCount());
        assertFalse(groups.connected("L001", "L002"));
    }

    @Test
    public void ordinaryUse_joiningTwoGroupsMakesOne() {
        DisjointSet<String> groups = new DisjointSet<>();
        groups.makeSet("A");
        groups.makeSet("B");
        groups.makeSet("C");

        assertTrue(groups.union("A", "B"));
        assertTrue(groups.connected("A", "B"));
        assertEquals(2, groups.componentCount());

        groups.union("B", "C");
        assertTrue(groups.connected("A", "C"));   // joined through B
        assertEquals(1, groups.componentCount());
    }

    @Test
    public void edge_joiningTwoThatAreAlreadyTogetherChangesNothing() {
        // This is exactly the check Kruskal makes. If both ends of a road are
        // already in the same group, adding that road would close a loop.
        DisjointSet<String> groups = new DisjointSet<>();
        groups.makeSet("A");
        groups.makeSet("B");
        groups.union("A", "B");

        assertFalse(groups.union("A", "B"));
        assertEquals(1, groups.componentCount());
    }

    @Test
    public void badInput_askingAboutSomethingNeverAddedFails() {
        final DisjointSet<String> groups = new DisjointSet<>();
        groups.makeSet("A");

        // Kruskal calls find() on both ends of every road. If a road names a
        // place that was never added, that is a data error, not a near miss.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                groups.find("never added");
            }
        });
    }
}
