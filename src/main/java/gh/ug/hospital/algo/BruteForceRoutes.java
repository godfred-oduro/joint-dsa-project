package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22399010
 *
 * Lists every possible route between two places, not just the best one.
 *
 * Dijkstra gives one answer, the quickest route. Sometimes you want all of
 * them: which routes avoid the paediatric unit, which stay under cover, which
 * are still open at night. To answer those you have to look at every route.
 *
 * The method is simple. Walk out from the start. At each place, try every
 * path leading out of it, as long as it does not take us somewhere already on
 * the current route. When we reach the destination, record the route and back
 * up to try something else.
 *
 * This is genuinely slow. The number of routes can grow like a factorial, so
 * a map with no hop limit could keep this running for hours. That is the
 * point: it shows why the clever algorithms exist. The hop limit keeps it
 * usable for the demonstration.
 *
 * The set is what stops us walking in circles. It remembers which places are
 * already on the route we are currently building.
 *
 * Speed: O(places factorial) in the worst case. Use the hop limit.
 */
public class BruteForceRoutes {

    /** One complete route, with what it costs. */
    public static class Route {
        public final DynamicArray<String> places;
        public final double minutes;

        Route(DynamicArray<String> places, double minutes) {
            this.places = places;
            this.minutes = minutes;
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < places.size(); i++) {
                if (i > 0) {
                    out.append(" -> ");
                }
                out.append(places.get(i));
            }
            out.append("  (").append(String.format("%.2f", minutes)).append(" min)");
            return out.toString();
        }
    }

    public static AlgorithmResult run(GraphView graph, String from, String to,
                                      int maximumHops) {
        return run(graph, from, to, maximumHops, false);
    }

    public static AlgorithmResult run(GraphView graph, String from, String to,
                                      int maximumHops, boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        DynamicArray<Route> found = new DynamicArray<>();
        DynamicArray<String> workingRoute = new DynamicArray<>();
        HashBackedSet<String> onRoute = new HashBackedSet<>();

        workingRoute.add(from);
        onRoute.add(from);

        explore(graph, from, to, maximumHops, workingRoute, onRoute,
                0.0, found, result, withTrace);

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(found);
        lastRouteCount = found.size();
        return result;
    }

    private static void explore(GraphView graph, String here, String target,
                                int hopsLeft, DynamicArray<String> workingRoute,
                                HashBackedSet<String> onRoute, double costSoFar,
                                DynamicArray<Route> found, AlgorithmResult result,
                                boolean withTrace) {

        result.countComparison();

        if (here.equals(target)) {
            // Copy the route, because workingRoute keeps changing underneath us.
            DynamicArray<String> snapshot = new DynamicArray<>();
            for (int i = 0; i < workingRoute.size(); i++) {
                snapshot.add(workingRoute.get(i));
            }
            found.add(new Route(snapshot, costSoFar));

            if (withTrace) {
                result.addTrace("reached " + target,
                        snapshot.toString(),
                        "route " + found.size() + " found, "
                                + String.format("%.2f", costSoFar) + " min");
            }
            return;
        }

        if (hopsLeft <= 0) {
            return;     // gone far enough down this branch
        }

        DynamicArray<String> nextDoor = graph.neighbours(here);
        for (int i = 0; i < nextDoor.size(); i++) {
            String neighbour = nextDoor.get(i);

            if (onRoute.contains(neighbour)) {
                continue;   // already on this route, going there would loop
            }

            workingRoute.add(neighbour);
            onRoute.add(neighbour);
            result.countSwap();

            explore(graph, neighbour, target, hopsLeft - 1, workingRoute, onRoute,
                    costSoFar + graph.edgeWeight(here, neighbour),
                    found, result, withTrace);

            // Undo, so the next branch starts clean. This is backtracking.
            workingRoute.remove(workingRoute.size() - 1);
            onRoute.remove(neighbour);
        }
    }

    private static int lastRouteCount;

    public static int lastRouteCount() {
        return lastRouteCount;
    }

    /**
     * The cheapest of all the routes found. Should match what Dijkstra says,
     * and comparing the two is a good test that both are right.
     */
    public static Route cheapestOf(DynamicArray<Route> routes) {
        if (routes.size() == 0) {
            return null;
        }
        Route best = routes.get(0);
        for (int i = 1; i < routes.size(); i++) {
            if (routes.get(i).minutes < best.minutes) {
                best = routes.get(i);
            }
        }
        return best;
    }
}
