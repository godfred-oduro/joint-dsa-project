package gh.ug.hospital.algo;

import gh.ug.hospital.ds.BinaryHeap;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.exception.StructureException;
import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22248029
 *
 * Finds the quickest route across the hospital, in minutes.
 *
 * BFS finds the route with the fewest corridors. That is not the same thing.
 * Two long uncovered paths across open ground take longer than four short
 * covered ones. Dijkstra works with the actual cost of each path, so it
 * answers the question a porter actually cares about.
 *
 * How it works. Keep a running best-known time to every place, starting at
 * zero for where we are and infinity everywhere else. Repeatedly take the
 * unfinished place with the smallest known time, and check whether going
 * through it gives a better time to each of its neighbours. That check is
 * called relaxing an edge. Once a place is taken, its time is final.
 *
 * Why the time is final: every path cost is positive, so any other route to
 * that place would have to pass through some unfinished place, and every
 * unfinished place already has a time at least as large. There is no way
 * back down. This is also why Dijkstra breaks if any cost is negative.
 *
 * The heap is what keeps it fast. Without one we would scan every place at
 * every step. With one, "give me the smallest" costs O(log n).
 *
 * Speed: O((places + paths) log places).
 */
public class Dijkstra {

    /** One place waiting in the heap, with the best time known so far. */
    private static class Waiting {
        final String place;
        final double minutes;

        Waiting(String place, double minutes) {
            this.place = place;
            this.minutes = minutes;
        }

        @Override
        public String toString() {
            return place + "(" + String.format("%.2f", minutes) + ")";
        }
    }

    public static AlgorithmResult run(GraphView graph, String from, String to) {
        return run(graph, from, to, false);
    }

    public static AlgorithmResult run(GraphView graph, String from, String to,
                                      boolean withTrace) {
        if (!graph.hasVertex(from)) {
            throw new StructureException("The graph has no place called " + from);
        }
        if (!graph.hasVertex(to)) {
            throw new StructureException("The graph has no place called " + to);
        }

        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        HashBackedMap<String, Double> bestTime = new HashBackedMap<>();
        HashBackedMap<String, String> cameFrom = new HashBackedMap<>();
        HashBackedSet<String> settled = new HashBackedSet<>();

        // Everywhere starts unreachable except where we are standing.
        DynamicArray<String> allPlaces = graph.vertices();
        for (int i = 0; i < allPlaces.size(); i++) {
            bestTime.put(allPlaces.get(i), Double.valueOf(Double.POSITIVE_INFINITY));
        }
        bestTime.put(from, Double.valueOf(0.0));

        Comparator<Waiting> soonestFirst = new Comparator<Waiting>() {
            @Override
            public int compare(Waiting a, Waiting b) {
                return Double.compare(a.minutes, b.minutes);
            }
        };

        BinaryHeap<Waiting> frontier = new BinaryHeap<>(soonestFirst);
        frontier.insert(new Waiting(from, 0.0));

        while (!frontier.isEmpty()) {
            Waiting current = frontier.extract();

            // A place can sit in the heap more than once, with an old worse
            // time. If we have already settled it, that copy is stale.
            if (settled.contains(current.place)) {
                continue;
            }
            settled.add(current.place);

            if (withTrace) {
                String[] extras = {
                        frontier.toString(),
                        String.format("%.2f", current.minutes),
                        readPrevious(cameFrom, current.place),
                        String.valueOf(settled.size())
                };
                result.addTrace("settle " + current.place,
                        "best time " + String.format("%.2f", current.minutes) + " min",
                        "this time can no longer improve",
                        extras);
            }

            if (current.place.equals(to)) {
                break;      // we have the answer, no need to settle the rest
            }

            DynamicArray<String> nextDoor = graph.neighbours(current.place);
            for (int i = 0; i < nextDoor.size(); i++) {
                String neighbour = nextDoor.get(i);
                if (settled.contains(neighbour)) {
                    continue;
                }

                double stepCost = graph.edgeWeight(current.place, neighbour);
                double throughHere = current.minutes + stepCost;

                result.countComparison();
                if (throughHere < bestTime.get(neighbour).doubleValue()) {
                    bestTime.put(neighbour, Double.valueOf(throughHere));
                    cameFrom.put(neighbour, current.place);
                    frontier.insert(new Waiting(neighbour, throughHere));
                    result.countSwap();     // one edge relaxed
                }
            }
        }

        // Walk the trail of "who did I come from" backwards, then flip it.
        DynamicArray<String> route = new DynamicArray<>();
        double total = bestTime.get(to).doubleValue();

        if (total != Double.POSITIVE_INFINITY) {
            DynamicArray<String> backwards = new DynamicArray<>();
            String walker = to;
            while (walker != null) {
                backwards.add(walker);
                walker = cameFrom.get(walker);
            }
            for (int i = backwards.size() - 1; i >= 0; i--) {
                route.add(backwards.get(i));
            }
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(route);
        lastTotalMinutes = total;
        lastBestTimes = bestTime;
        return result;
    }

    private static double lastTotalMinutes;
    private static HashBackedMap<String, Double> lastBestTimes;

    /** How long the route found by the last run takes, in minutes. */
    public static double lastTotalMinutes() {
        return lastTotalMinutes;
    }

    /** True if there was no route at all. */
    public static boolean lastRunFoundNoRoute() {
        return lastTotalMinutes == Double.POSITIVE_INFINITY;
    }

    public static HashBackedMap<String, Double> lastBestTimes() {
        return lastBestTimes;
    }

    private static String readPrevious(HashBackedMap<String, String> cameFrom,
                                       String place) {
        String previous = cameFrom.get(place);
        if (previous == null) {
            return "-";
        }
        return previous;
    }

    public static String[] traceColumns() {
        return new String[]{"Frontier", "Best Time", "Came From", "Settled"};
    }
}
