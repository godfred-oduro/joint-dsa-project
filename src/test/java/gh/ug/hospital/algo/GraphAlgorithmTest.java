package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.graph.AdjacencyListGraph;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owners: 22372639 (BFS), 22372639 (DFS), 22248029 (Dijkstra),
 *         22104879 (Prim), 22040255 (Kruskal), 22399010 (brute force)
 *
 * Tests for the six algorithms that walk the hospital map.
 *
 * They share a class because they all need the same map to work on, and
 * several of the interesting checks compare one against another.
 */
/**
 * Owner: 22372639
 *
 * Tests for the graph algorithms: both traversals, shortest path and spanning trees.
 */
public class GraphAlgorithmTest {

    /**
     * A small map with a deliberate trap in it.
     *
     *   L001 --0.5-- L002 --0.5-- L003
     *     \                        /
     *      \--------10.0----------/
     *
     * Going straight from L001 to L003 is one step but costs 10 minutes.
     * Going round through L002 is two steps but costs 1 minute. BFS counts
     * steps and picks the wrong one. Dijkstra counts minutes and picks right.
     */
    private GraphView trapMap() {
        AdjacencyListGraph map = new AdjacencyListGraph();
        map.addVertex("L001");
        map.addVertex("L002");
        map.addVertex("L003");
        map.addEdge("L001", "L002", 0.5);
        map.addEdge("L002", "L003", 0.5);
        map.addEdge("L001", "L003", 10.0);
        return map;
    }

    private GraphView splitMap() {
        AdjacencyListGraph map = new AdjacencyListGraph();
        map.addVertex("A");
        map.addVertex("B");
        map.addVertex("FAR");     // nothing joins to this one
        map.addEdge("A", "B", 1.0);
        return map;
    }

    @SuppressWarnings("unchecked")
    private DynamicArray<String> namesFrom(AlgorithmResult result) {
        return (DynamicArray<String>) result.getAnswer();
    }

    @Test
    public void ordinaryUse_bfsAndDfsBothReachEverything() {
        GraphView map = trapMap();

        DynamicArray<String> bfsOrder = namesFrom(BreadthFirstSearch.run(map, "L001"));
        DynamicArray<String> dfsOrder = namesFrom(DepthFirstSearch.run(map, "L001"));

        assertEquals(3, bfsOrder.size());
        assertEquals(3, dfsOrder.size());
        assertEquals("L001", bfsOrder.get(0));
        assertEquals("L001", dfsOrder.get(0));
    }

    @Test
    public void ordinaryUse_dijkstraTakesTheLongWayRoundWhenItIsQuicker() {
        // This is the whole reason Dijkstra exists in this project. Fewest
        // corridors is not the same as quickest, and a porter cares about
        // minutes, not corridors.
        GraphView map = trapMap();

        AlgorithmResult route = Dijkstra.run(map, "L001", "L003");
        DynamicArray<String> path = namesFrom(route);

        assertEquals(3, path.size(), "Dijkstra took the one-step 10 minute path");
        assertEquals("L001", path.get(0));
        assertEquals("L002", path.get(1));
        assertEquals("L003", path.get(2));
        assertEquals(1.0, Dijkstra.lastTotalMinutes(), 0.0001);
    }

    @Test
    public void ordinaryUse_bothSpanningTreesGiveTheSameTotal() {
        // Prim and Kruskal work completely differently. Prim grows one tree
        // outward; Kruskal sorts every path and takes the cheap ones that do
        // not close a loop. They can pick different paths when weights tie,
        // but the total must match.
        GraphView map = trapMap();

        Kruskal.run(map);
        double kruskalTotal = Kruskal.lastTotalMinutes();

        Prim.run(map, "L001");
        double primTotal = Prim.lastTotalMinutes();

        assertEquals(kruskalTotal, primTotal, 0.0001,
                "the two spanning trees disagree, so one of them is wrong");
        assertEquals(1.0, kruskalTotal, 0.0001);
    }

    @Test
    public void edge_disconnectedGraphLeavesSomePlacesUnreachable() {
        // Real hospital maps should never actually be disconnected (that is
        // constraint C17, checked separately by
        // BreadthFirstSearch.isWholeGraphReachable), but the algorithms
        // still have to behave sanely if a location genuinely has no path
        // in - a newly added ward before its corridor is on the map, say.
        GraphView map = splitMap();   // A-B joined, FAR sits on its own

        DynamicArray<String> reachable = namesFrom(BreadthFirstSearch.run(map, "A"));
        assertEquals(2, reachable.size(), "FAR should not be reachable from A");
        assertFalse(BreadthFirstSearch.isWholeGraphReachable(map, "A"));

        // Asking for a route to a real, known place that just is not
        // connected must not throw and must not invent a path - it is a
        // different situation from badInput_routingFromAPlaceTheMap
        // DoesNotKnowFails below, where the place itself is not even on
        // the map.
        AlgorithmResult route = Dijkstra.run(map, "A", "FAR");
        assertEquals(0, namesFrom(route).size(), "there is no path to FAR");
        assertEquals(Double.POSITIVE_INFINITY, Dijkstra.lastTotalMinutes(), 0.0001);
    }

    @Test
    public void badInput_routingFromAPlaceTheMapDoesNotKnowFails() {
        final AdjacencyListGraph map = new AdjacencyListGraph();
        map.addVertex("L001");
        map.addVertex("L015");
        map.addEdge("L001", "L015", 1.0);

        // A typed location code that does not exist would otherwise give a
        // silent "no route found", which hides the real problem.
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                Dijkstra.run(map, "L999", "L015");
            }
        });
    }
}
