package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.LinkedQueue;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22399601
 *
 * Explores the hospital map ring by ring, spreading out from a starting point.
 *
 * Visit the start. Then everywhere one step away. Then everywhere two steps
 * away. And so on. It never goes deep before it has finished the current
 * ring, which is why it finds the route with the fewest steps.
 *
 * The queue is what makes this work, and it is the queue this same member
 * wrote. Places waiting to be visited join the back; we always take from the
 * front. Swap the queue for a stack and you get depth-first search instead,
 * which is a nice thing to point out in the defence.
 *
 * Careful: fewest steps is not the same as fastest. A route of two long
 * outdoor paths takes longer than four short covered ones. For time you need
 * Dijkstra, which takes the weights into account. BFS answers "which wards
 * can be reached at all" and "how many corridors away is the theatre".
 *
 * Speed: O(places + paths).
 */
public class BreadthFirstSearch {

    public static AlgorithmResult run(GraphView graph, String start) {
        return run(graph, start, false);
    }

    public static AlgorithmResult run(GraphView graph, String start, boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        HashBackedSet<String> visited = new HashBackedSet<>();
        HashBackedMap<String, Integer> levelOf = new HashBackedMap<>();
        DynamicArray<String> visitOrder = new DynamicArray<>();
        LinkedQueue<String> waiting = new LinkedQueue<>();

        visited.add(start);
        levelOf.put(start, Integer.valueOf(0));
        waiting.enqueue(start);

        while (!waiting.isEmpty()) {
            String here = waiting.dequeue();
            visitOrder.add(here);
            int myLevel = levelOf.get(here).intValue();

            if (withTrace) {
                result.addTrace("visit " + here,
                        "queue: " + waiting,
                        "this place is " + myLevel + " step(s) from " + start);
            }

            DynamicArray<String> nextDoor = graph.neighbours(here);
            for (int i = 0; i < nextDoor.size(); i++) {
                String neighbour = nextDoor.get(i);
                result.countComparison();

                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    levelOf.put(neighbour, Integer.valueOf(myLevel + 1));
                    waiting.enqueue(neighbour);
                    result.countSwap();
                }
            }
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(visitOrder);

        // Keep the levels where the menu can read them.
        lastLevels = levelOf;
        lastReachable = visited;
        return result;
    }

    private static HashBackedMap<String, Integer> lastLevels;
    private static HashBackedSet<String> lastReachable;

    /** How many steps each place was from the start, after the last run. */
    public static HashBackedMap<String, Integer> lastLevels() {
        return lastLevels;
    }

    /** Every place reachable from the start, after the last run. */
    public static HashBackedSet<String> lastReachable() {
        return lastReachable;
    }

    /**
     * True if every place in the graph can be reached from the start.
     * Constraint C17 says our hospital map must pass this.
     */
    public static boolean isWholeGraphReachable(GraphView graph, String start) {
        run(graph, start, false);
        return lastReachable.size() == graph.vertexCount();
    }
}
