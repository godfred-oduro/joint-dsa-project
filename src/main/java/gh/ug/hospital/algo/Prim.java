package gh.ug.hospital.algo;

import gh.ug.hospital.ds.BinaryHeap;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;

import java.util.Comparator;

/**
 * Owner: 22104879
 *
 * Finds the cheapest set of paths that still connects every place.
 *
 * The question this answers: if we had to light or resurface the smallest
 * possible set of paths and still let a porter reach every ward, which paths
 * would we choose? That set is called a minimum spanning tree.
 *
 * Prim's method grows one tree outward. Start anywhere. Look at every path
 * leading out of the tree so far, take the cheapest, and add the place at
 * its far end. Repeat until every place is in.
 *
 * It is greedy, and here greed is provably right. If you take the cheapest
 * path leaving the tree, that path must be in some minimum spanning tree,
 * because swapping it for any other path crossing the same boundary could
 * only make the total worse. That argument is called the cut property.
 *
 * Kruskal answers the same question a different way, sorting all the paths
 * first. Prim suits dense maps, Kruskal suits sparse ones. Our hospital map
 * is sparse, so Kruskal should win on time, and experiment E6 checks that.
 *
 * Speed: O(paths log places) with a heap.
 */
public class Prim {

    /** One path being considered, from a place in the tree to one outside. */
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

    public static AlgorithmResult run(GraphView graph, String startPlace) {
        return run(graph, startPlace, false);
    }

    public static AlgorithmResult run(GraphView graph, String startPlace,
                                      boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        HashBackedSet<String> inTree = new HashBackedSet<>();
        DynamicArray<Path> chosen = new DynamicArray<>();
        double totalMinutes = 0.0;

        Comparator<Path> cheapestFirst = new Comparator<Path>() {
            @Override
            public int compare(Path a, Path b) {
                return Double.compare(a.minutes, b.minutes);
            }
        };

        BinaryHeap<Path> options = new BinaryHeap<>(cheapestFirst);

        inTree.add(startPlace);
        addPathsLeaving(graph, startPlace, inTree, options, result);

        while (!options.isEmpty() && inTree.size() < graph.vertexCount()) {
            Path cheapest = options.extract();
            result.countComparison();

            // Both ends already in the tree means this path would make a loop.
            if (inTree.contains(cheapest.to)) {
                if (withTrace) {
                    String[] extras = {
                            cheapest.toString(),
                            String.format("%.2f", cheapest.minutes),
                            "no",
                            String.valueOf(inTree.size())
                    };
                    result.addTrace("consider " + cheapest,
                            "tree has " + inTree.size() + " places",
                            "skipped, both ends are already in the tree",
                            extras);
                }
                continue;
            }

            inTree.add(cheapest.to);
            chosen.add(cheapest);
            totalMinutes += cheapest.minutes;
            result.countSwap();

            if (withTrace) {
                String[] extras = {
                        cheapest.toString(),
                        String.format("%.2f", cheapest.minutes),
                        "yes",
                        String.valueOf(inTree.size())
                };
                result.addTrace("take " + cheapest,
                        "running total " + String.format("%.2f", totalMinutes) + " min",
                        "added " + cheapest.to + " to the tree",
                        extras);
            }

            addPathsLeaving(graph, cheapest.to, inTree, options, result);
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(chosen);
        lastTotalMinutes = totalMinutes;
        lastPlacesReached = inTree.size();
        return result;
    }

    private static void addPathsLeaving(GraphView graph, String place,
                                        HashBackedSet<String> inTree,
                                        BinaryHeap<Path> options,
                                        AlgorithmResult result) {
        DynamicArray<String> nextDoor = graph.neighbours(place);
        for (int i = 0; i < nextDoor.size(); i++) {
            String neighbour = nextDoor.get(i);
            if (!inTree.contains(neighbour)) {
                options.insert(new Path(place, neighbour,
                        graph.edgeWeight(place, neighbour)));
            }
        }
    }

    private static double lastTotalMinutes;
    private static int lastPlacesReached;

    public static double lastTotalMinutes() {
        return lastTotalMinutes;
    }

    /**
     * If this is smaller than the number of places, the map is in separate
     * pieces and constraint C17 has been broken.
     */
    public static int lastPlacesReached() {
        return lastPlacesReached;
    }

    public static String[] traceColumns() {
        return new String[]{"Path Considered", "Minutes", "Accepted?", "Places In Tree"};
    }
}
