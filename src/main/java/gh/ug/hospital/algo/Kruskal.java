package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DisjointSet;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22040255
 *
 * The other way to find the cheapest set of paths that connects everything.
 *
 * Prim grows one tree outward from a starting place. Kruskal ignores where
 * things are entirely: sort every path in the hospital from cheapest to
 * dearest, then walk down that list taking any path whose two ends are not
 * already connected. Skip the ones that would make a loop.
 *
 * The only hard part is answering "are these two already connected?" quickly,
 * and that is exactly what the disjoint set does. Each place starts in its
 * own group. Taking a path joins two groups. If both ends are already in the
 * same group, the path would close a loop, so we skip it.
 *
 * Watching the group count fall from 52 down to 1 is the evidence this file
 * owes the report, and it shows the disjoint set doing real work.
 *
 * Because our map is sparse, 52 places and only 116 paths, sorting the paths
 * is cheap and Kruskal should beat Prim here. Experiment E6 checks it.
 *
 * Speed: O(paths log paths), which the sort dominates.
 */
public class Kruskal {

    public static class Path {
        public final String from;
        public final String to;
        public final double minutes;

        Path(String from, String to, double minutes) {
            this.from = from;
            this.to = to;
            this.minutes = minutes;
        }

        @Override
        public String toString() {
            return from + "-" + to + "(" + String.format("%.2f", minutes) + ")";
        }
    }

    public static AlgorithmResult run(GraphView graph) {
        return run(graph, false);
    }

    public static AlgorithmResult run(GraphView graph, boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        // Collect every path once. The graph stores each one in both
        // directions, so only keep the copy where from is smaller than to.
        DynamicArray<Path> everyPath = new DynamicArray<>();
        DynamicArray<String> places = graph.vertices();

        for (int i = 0; i < places.size(); i++) {
            String here = places.get(i);
            DynamicArray<String> nextDoor = graph.neighbours(here);
            for (int j = 0; j < nextDoor.size(); j++) {
                String there = nextDoor.get(j);
                if (here.compareTo(there) < 0) {
                    everyPath.add(new Path(here, there, graph.edgeWeight(here, there)));
                }
            }
        }

        // Sort them cheapest first, reusing the group's own merge sort.
        Path[] sorted = new Path[everyPath.size()];
        for (int i = 0; i < everyPath.size(); i++) {
            sorted[i] = everyPath.get(i);
        }

        Comparator<Path> cheapestFirst = new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                return Double.compare(a.minutes, b.minutes);
            }
        };

        AlgorithmResult sortWork = MergeSort.run(sorted, cheapestFirst);
        result.addComparisons(sortWork.getComparisons());
        result.addSwaps(sortWork.getSwaps());

        // Every place starts in a group of its own.
        DisjointSet<String> groups = new DisjointSet<>();
        for (int i = 0; i < places.size(); i++) {
            groups.makeSet(places.get(i));
        }

        DynamicArray<Path> chosen = new DynamicArray<>();
        double totalMinutes = 0.0;

        for (int i = 0; i < sorted.length; i++) {
            Path path = sorted[i];
            result.countComparison();

            boolean joined = groups.union(path.from, path.to);

            if (joined) {
                chosen.add(path);
                totalMinutes += path.minutes;
                result.countSwap();
            }

            if (withTrace) {
                String accepted;
                if (joined) {
                    accepted = "yes";
                } else {
                    accepted = "no, would make a loop";
                }
                String[] extras = {
                        path.toString(),
                        String.format("%.2f", path.minutes),
                        accepted,
                        String.valueOf(groups.componentCount())
                };
                result.addTrace("consider " + path,
                        "running total " + String.format("%.2f", totalMinutes) + " min",
                        "groups left: " + groups.componentCount(),
                        extras);
            }

            // Once everything is in one group we are finished.
            if (groups.componentCount() == 1) {
                break;
            }
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(chosen);
        lastTotalMinutes = totalMinutes;
        lastGroupCount = groups.componentCount();
        lastCompressions = groups.getCompressions();
        return result;
    }

    private static double lastTotalMinutes;
    private static int lastGroupCount;
    private static long lastCompressions;

    public static double lastTotalMinutes() {
        return lastTotalMinutes;
    }

    /** Should be 1. Anything more means the map is in separate pieces (C17). */
    public static int lastGroupCount() {
        return lastGroupCount;
    }

    /** How many nodes path compression re-pointed. Evidence for the trace. */
    public static long lastCompressions() {
        return lastCompressions;
    }

    public static String[] traceColumns() {
        return new String[]{"Path Considered", "Minutes", "Accepted?", "Groups Left"};
    }
}
