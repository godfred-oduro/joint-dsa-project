package gh.ug.hospital.ops;

import gh.ug.hospital.algo.TopologicalSort;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22015166
 *
 * Puts referrals in a workable order. Rule BR5.
 *
 * The hospital has a firm rule, published on its own website: a patient
 * cannot walk straight into a specialist clinic. They see a general
 * practitioner first, the GP writes a referral note, and only then are they
 * given an appointment on that specialist's clinic day.
 *
 * That means some steps must happen before others. Draw an arrow from each
 * step to the step that depends on it and you get a graph. Working out an
 * order that respects every arrow is called a topological sort.
 *
 * If the graph has a loop, no order works: A waits for B, B waits for C, and
 * C waits for A. That means the data is wrong, so we stop and say so rather
 * than producing a nonsense order.
 */
public class ReferralOrdering {

    /** Steps are named like this so the trace tables read clearly. */
    public static String gpStepFor(ServiceRequest request) {
        return request.getRequestId() + ":GP";
    }

    public static String specialistStepFor(ServiceRequest request) {
        return request.getRequestId() + ":" + request.getTargetClinic();
    }

    /**
     * Builds the "must happen before" list from a set of requests.
     *
     * Note this is not the hospital map. The map's paths work in both
     * directions, but "see the GP first" only works one way, so the
     * topological sort keeps its own one-way structure.
     *
     * Only requests that need a GP referral add an arrow. Everything else is
     * a single step with nothing in front of it.
     */
    public static TopologicalSort.Precedence buildPrecedence(
            DynamicArray<ServiceRequest> requests) {

        TopologicalSort.Precedence work = new TopologicalSort.Precedence();

        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest request = requests.get(i);

            if (request.requiresGpReferral()) {
                // The arrow means: see the GP before the specialist.
                work.mustComeBefore(gpStepFor(request), specialistStepFor(request));
            } else {
                work.addJob(request.getRequestId() + ":DIRECT");
            }
        }
        return work;
    }

    /**
     * Works out a valid order for all the steps.
     *
     * Throws DomainException if the referral data contains a loop,
     * because then no order can possibly work.
     */
    public static AlgorithmResult order(DynamicArray<ServiceRequest> requests) {
        return TopologicalSort.run(buildPrecedence(requests), true);
    }

    /**
     * Which requests still need their GP visit before they can go further.
     */
    public static DynamicArray<ServiceRequest> awaitingGpVisit(
            DynamicArray<ServiceRequest> requests) {

        DynamicArray<ServiceRequest> waiting = new DynamicArray<>();
        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest request = requests.get(i);
            if (request.requiresGpReferral()
                    && ServiceRequest.NEW.equals(request.getStatus())) {
                waiting.add(request);
            }
        }
        return waiting;
    }

    /**
     * Counts how many requests of each kind there are, so the report can say
     * how much of the workload the rule actually touches.
     */
    public static String summarise(DynamicArray<ServiceRequest> requests) {
        int needReferral = 0;
        int direct = 0;

        HashBackedMap<String, Integer> byClinic = new HashBackedMap<>();

        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest request = requests.get(i);
            if (request.requiresGpReferral()) {
                needReferral++;
                String clinic = request.getTargetClinic();
                Integer soFar = byClinic.get(clinic);
                if (soFar == null) {
                    byClinic.put(clinic, Integer.valueOf(1));
                } else {
                    byClinic.put(clinic, Integer.valueOf(soFar.intValue() + 1));
                }
            } else {
                direct++;
            }
        }

        StringBuilder out = new StringBuilder();
        out.append(needReferral).append(" requests need a GP visit first, ");
        out.append(direct).append(" go straight through.\n");
        out.append("By clinic: ");

        DynamicArray<String> clinics = byClinic.keys();
        for (int i = 0; i < clinics.size(); i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(clinics.get(i)).append(" ").append(byClinic.get(clinics.get(i)));
        }
        return out.toString();
    }
}
