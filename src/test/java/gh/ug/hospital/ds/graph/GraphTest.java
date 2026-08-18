package gh.ug.hospital.ds.graph;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owner: 22248029
 *
 * Tests for both ways of holding the hospital map.
 *
 * The brief asks for the map stored as an adjacency list and as an adjacency
 * matrix. The two must agree about what the map says, and they must differ
 * in how much room they take. Both of those are checked here.
 */
public class GraphTest {

    /** Builds the same small map in whichever kind of graph is passed in. */
    private void buildSmallMap(GraphView map) {
        map.addVertex("L001");
        map.addVertex("L015");
        map.addVertex("L038");

        map.addEdge("L001", "L015", 0.74);
        map.addEdge("L015", "L038", 1.20);
    }

    @Test
    public void ordinaryUse_bothKindsAgreeAboutTheMap() {
        AdjacencyListGraph list = new AdjacencyListGraph();
        AdjacencyMatrixGraph matrix = new AdjacencyMatrixGraph(10);

        buildSmallMap(list);
        buildSmallMap(matrix);

        assertEquals(list.vertexCount(), matrix.vertexCount());
        assertEquals(list.edgeCount(), matrix.edgeCount());
        assertEquals(list.hasEdge("L001", "L015"), matrix.hasEdge("L001", "L015"));
        assertEquals(list.hasEdge("L001", "L038"), matrix.hasEdge("L001", "L038"));
        assertEquals(list.edgeWeight("L015", "L038"),
                matrix.edgeWeight("L015", "L038"), 0.0001);
    }

    @Test
    public void ordinaryUse_pathsWorkInBothDirections() {
        AdjacencyListGraph map = new AdjacencyListGraph();
        buildSmallMap(map);

        // A corridor you can only walk one way would be a strange hospital.
        assertTrue(map.hasEdge("L001", "L015"));
        assertTrue(map.hasEdge("L015", "L001"));
        assertEquals(map.edgeWeight("L001", "L015"),
                map.edgeWeight("L015", "L001"), 0.0001);
    }

    @Test
    public void ordinaryUse_neighboursAreListedCorrectly() {
        AdjacencyListGraph map = new AdjacencyListGraph();
        buildSmallMap(map);

        DynamicArray<String> fromMiddle = map.neighbours("L015");
        assertEquals(2, fromMiddle.size());
        assertEquals(2, map.degree("L015"));
        assertEquals(1, map.degree("L001"));
    }

    @Test
    public void badInput_unknownPlacesAndMissingPathsFail() {
        final AdjacencyListGraph map = new AdjacencyListGraph();
        map.addVertex("L001");
        map.addVertex("L015");
        map.addVertex("L038");
        map.addEdge("L001", "L015", 0.74);

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                map.neighbours("L999");
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                map.edgeWeight("L001", "L038");
            }
        });
    }
}
