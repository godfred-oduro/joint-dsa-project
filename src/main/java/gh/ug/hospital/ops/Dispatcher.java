package gh.ug.hospital.ops;

import gh.ug.hospital.algo.Dijkstra;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.model.Resource;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.AlgorithmResult;

import java.time.LocalDateTime;

/**
 * Owner: 22160537
 *
 * Chooses which porter, wheelchair or ambulance takes a job. Rules BR9 and BR10.
 *
 * The method is greedy: among everything of the right kind that is free, on
 * shift and not already at capacity, pick whichever is nearest to where the
 * job starts. Then move on to the next job and do the same again.
 *
 * Greedy is quick and it is usually good. It is not always best, and showing
 * where it fails is one of our two required counterexamples. The class
 * GreedyAssignment holds that demonstration.
 *
 * The ambulance rule is separate and worth reading. The hospital has two
 * ambulances. When a third emergency arrives and both are out, we do not
 * fail. Ghana has a free national ambulance service reachable on 112, so the
 * job is passed to them and the escalation is written into the audit log.
 * That is what really happens, and it gives the algorithm a proper fallback
 * tier instead of an error.
 */
public class Dispatcher {

    /** The emergency number the escalation goes to. */
    public static final String NATIONAL_AMBULANCE_NUMBER = "112";

    private final GraphView map;
    private final DynamicArray<Resource> resources;

    /** How many times we had to call 112 because both ambulances were out. */
    private int escalationCount;

    public Dispatcher(GraphView map, DynamicArray<Resource> resources) {
        this.map = map;
        this.resources = resources;
        this.escalationCount = 0;
    }

    /**
     * Picks the nearest suitable resource for a job.
     *
     * @param request       the job
     * @param resourceType  what kind of resource it needs
     * @param now           the time, which decides who is on shift
     */
    public AlgorithmResult assign(ServiceRequest request, String resourceType,
                                  LocalDateTime now) {
        AlgorithmResult result = new AlgorithmResult();
        long startedAt = System.nanoTime();

        String startPoint = request.getSourceLocationId();

        Resource best = null;
        double bestCost = Double.POSITIVE_INFINITY;
        int considered = 0;

        for (int i = 0; i < resources.size(); i++) {
            Resource candidate = resources.get(i);

            if (!resourceType.equals(candidate.getResourceType())) {
                continue;
            }

            considered++;
            result.countComparison();

            // Rules C9 and C10: on shift, free, and with room for another job.
            if (!candidate.canTakeWork()) {
                result.addTrace("check " + candidate.getResourceId(),
                        candidate.getAvailabilityStatus(),
                        "skipped: already at " + candidate.getCurrentLoad()
                                + " of " + candidate.getCapacity());
                continue;
            }
            if (!candidate.isOnShiftAt(now.toLocalTime())) {
                result.addTrace("check " + candidate.getResourceId(),
                        candidate.getShift(),
                        "skipped: not on shift at " + now.toLocalTime());
                continue;
            }

            // How far away is it? Dijkstra gives the real travel time.
            double cost = travelCost(candidate.getHomeLocationId(), startPoint);

            result.addTrace("check " + candidate.getResourceId(),
                    candidate.getHomeLocationId(),
                    String.format("%.2f min away", cost));

            if (cost < bestCost) {
                bestCost = cost;
                best = candidate;
            }
        }

        if (best == null) {
            // Ambulances get the special fallback. Everything else simply fails.
            if (Resource.AMBULANCE.equals(resourceType)) {
                escalationCount++;
                result.addTrace("no ambulance free", "both are out",
                        "escalating to the national service on "
                                + NATIONAL_AMBULANCE_NUMBER);
                result.setElapsedNanos(System.nanoTime() - startedAt);
                result.setAnswer(null);
                throw new DomainException(
                        request.getRequestId() + ": both hospital ambulances are busy. "
                                + "Call " + NATIONAL_AMBULANCE_NUMBER
                                + " for the National Ambulance Service, which is free of charge.");
            }
            throw new DomainException(
                    request.getRequestId() + ": looked at " + considered + " "
                            + resourceType.toLowerCase()
                            + " resources and none is free and on shift at " + now + ".");
        }

        best.takeJob();
        request.setAssignedResourceId(best.getResourceId());
        request.setStatus(ServiceRequest.ASSIGNED);

        result.addTrace("assign", best.getResourceId(),
                String.format("chosen, %.2f min away", bestCost));

        result.setElapsedNanos(System.nanoTime() - startedAt);
        result.setAnswer(best);
        return result;
    }

    /** Travel time between two places, using the shortest route. */
    private double travelCost(String from, String to) {
        if (from == null || to == null) {
            return Double.POSITIVE_INFINITY;
        }
        if (from.equals(to)) {
            return 0.0;
        }
        if (!map.hasVertex(from) || !map.hasVertex(to)) {
            return Double.POSITIVE_INFINITY;
        }
        // Dijkstra hands back the list of places on the route. The time it
        // takes is read separately, straight afterwards.
        Dijkstra.run(map, from, to);
        return Dijkstra.lastTotalMinutes();
    }

    public int getEscalationCount() {
        return escalationCount;
    }

    /** Frees a resource up when its job is done. */
    public void finish(ServiceRequest request) {
        String resourceId = request.getAssignedResourceId();
        if (resourceId == null) {
            return;
        }
        for (int i = 0; i < resources.size(); i++) {
            if (resources.get(i).getResourceId().equals(resourceId)) {
                resources.get(i).finishJob();
                break;
            }
        }
        request.setStatus(ServiceRequest.COMPLETED);
    }
}
