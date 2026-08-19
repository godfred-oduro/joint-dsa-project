package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.LinkedStack;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22372639
 *
 * Explores the hospital map by following one route as far as it goes, then
 * backing up and trying the next.
 *
 * Where BFS spreads out in rings, this one commits. It walks from the main
 * gate down through OPD, into the corridor, into the ward, until there is
 * nowhere new to go, and only then backs up to try a turning it skipped.
 *
 * The only difference from BFS is the container. BFS uses a queue, so the
 * oldest waiting place is taken next. This uses a stack, so the newest one
 * is taken next, and that single change produces completely different
 * behaviour. It is the same stack this member wrote for the undo log.
 *
 * We also record when each place was first reached and when we finished with
 * it. Those two numbers are what topological sort and cycle detection are
 * built on.
 *
 * Speed: O(places + paths), the same as BFS.
 */
public class DepthFirstSearch {

    public static AlgorithmResult run(GraphView graph, String start) {
        return run(graph, start, false);
    }

    public static AlgorithmResult run(GraphView graph, String start, boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        HashBackedSet<String> visited = new HashBackedSet<>();
        HashBackedMap<String, Integer> discoveredAt = new HashBackedMap<>();
        HashBackedMap<String, Integer> finishedAt = new HashBackedMap<>();
        DynamicArray<String> visitOrder = new DynamicArray<>();
        LinkedStack<String> waiting = new LinkedStack<>();

        int clock = 0;
        waiting.push(start);

        while (!waiting.isEmpty()) {
            String here = waiting.pop();

            if (visited.contains(here)) {
                continue;   // we reached it by another route while it waited
            }

            visited.add(here);
            clock++;
            discoveredAt.put(here, Integer.valueOf(clock));
            visitOrder.add(here);

            if (withTrace) {
                result.addTrace("visit " + here,
                        "stack: " + waiting,
                        "first reached at time " + clock);
            }

            // Push the neighbours. The last one pushed is explored first,
            // which is what makes this go deep rather than wide.
            DynamicArray<String> nextDoor = graph.neighbours(here);
            for (int i = 0; i < nextDoor.size(); i++) {
                String neighbour = nextDoor.get(i);
                result.countComparison();
                if (!visited.contains(neighbour)) {
                    waiting.push(neighbour);
                    result.countSwap();
                }
            }

            clock++;
            finishedAt.put(here, Integer.valueOf(clock));
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(visitOrder);

        lastDiscovered = discoveredAt;
        lastFinished = finishedAt;
        lastReachable = visited;
        return result;
    }

    private static HashBackedMap<String, Integer> lastDiscovered;
    private static HashBackedMap<String, Integer> lastFinished;
    private static HashBackedSet<String> lastReachable;

    public static HashBackedMap<String, Integer> lastDiscoveryTimes() {
        return lastDiscovered;
    }

    public static HashBackedMap<String, Integer> lastFinishTimes() {
        return lastFinished;
    }

    public static HashBackedSet<String> lastReachable() {
        return lastReachable;
    }
}
