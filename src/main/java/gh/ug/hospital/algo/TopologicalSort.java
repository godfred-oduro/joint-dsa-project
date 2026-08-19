package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.ds.LinkedQueue;
import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22015166
 *
 * Puts jobs in an order that respects "this must happen before that".
 *
 * The hospital has a firm rule: a patient cannot walk into a specialist
 * clinic. They must see a general practitioner first, get a referral note,
 * and only then be given an appointment. So the general practitioner visit
 * must come before the specialist visit, always.
 *
 * Write those "must come before" links down and you get a graph with
 * direction. Topological sort produces an order in which no job appears
 * before something it depends on.
 *
 * The method used here is Kahn's. Count how many things each job is waiting
 * on. Anything waiting on nothing can be done now, so put those in a queue.
 * Take one out, add it to the order, and reduce the waiting count of
 * everything that depended on it. Anything that drops to zero joins the
 * queue.
 *
 * If the queue empties before every job is placed, the leftovers are waiting
 * on each other in a circle. In our data that means the referral rules are
 * wrong, so we stop loudly rather than produce a silently broken order.
 *
 * Speed: O(jobs + links).
 */
public class TopologicalSort {

    /**
     * A set of jobs with "must come before" links between them.
     * We build our own small graph here because the hospital map graph goes
     * both ways, and these links only go one way.
     */
    public static class Precedence {
        private final DynamicArray<String> jobs = new DynamicArray<>();
        private final HashBackedMap<String, DynamicArray<String>> comesAfter =
                new HashBackedMap<>();
        private final HashBackedMap<String, Integer> waitingOn = new HashBackedMap<>();

        public void addJob(String job) {
            if (comesAfter.containsKey(job)) {
                return;
            }
            jobs.add(job);
            comesAfter.put(job, new DynamicArray<String>());
            waitingOn.put(job, Integer.valueOf(0));
        }

        /** Says that "before" has to be finished before "after" can start. */
        public void mustComeBefore(String before, String after) {
            addJob(before);
            addJob(after);
            comesAfter.get(before).add(after);
            waitingOn.put(after, Integer.valueOf(waitingOn.get(after).intValue() + 1));
        }

        public DynamicArray<String> allJobs() {
            return jobs;
        }

        DynamicArray<String> after(String job) {
            return comesAfter.get(job);
        }

        int waitCount(String job) {
            return waitingOn.get(job).intValue();
        }
    }

    public static AlgorithmResult run(Precedence work) {
        return run(work, false);
    }

    public static AlgorithmResult run(Precedence work, boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        DynamicArray<String> jobs = work.allJobs();

        // Our own copy of the waiting counts, because we change them as we go.
        HashBackedMap<String, Integer> waiting = new HashBackedMap<>();
        for (int i = 0; i < jobs.size(); i++) {
            String job = jobs.get(i);
            waiting.put(job, Integer.valueOf(work.waitCount(job)));
        }

        LinkedQueue<String> readyNow = new LinkedQueue<>();
        for (int i = 0; i < jobs.size(); i++) {
            String job = jobs.get(i);
            if (waiting.get(job).intValue() == 0) {
                readyNow.enqueue(job);
            }
        }

        DynamicArray<String> order = new DynamicArray<>();

        while (!readyNow.isEmpty()) {
            String job = readyNow.dequeue();
            order.add(job);

            if (withTrace) {
                result.addTrace("place " + job,
                        "order so far: " + order,
                        "it was waiting on nothing");
            }

            DynamicArray<String> dependents = work.after(job);
            for (int i = 0; i < dependents.size(); i++) {
                String next = dependents.get(i);
                int stillWaiting = waiting.get(next).intValue() - 1;
                waiting.put(next, Integer.valueOf(stillWaiting));
                result.countComparison();

                if (stillWaiting == 0) {
                    readyNow.enqueue(next);
                    result.countSwap();
                }
            }
        }

        if (order.size() != jobs.size()) {
            StringBuilder stuck = new StringBuilder();
            for (int i = 0; i < jobs.size(); i++) {
                String job = jobs.get(i);
                if (waiting.get(job).intValue() > 0) {
                    if (stuck.length() > 0) {
                        stuck.append(", ");
                    }
                    stuck.append(job);
                }
            }
            throw new DomainException(
                    "These jobs are waiting on each other in a circle, so no valid "
                            + "order exists: " + stuck
                            + ". The referral data needs fixing.");
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(order);
        return result;
    }

    /**
     * Checks whether an order is valid, without trusting the sort that
     * produced it. Useful in the tests.
     */
    public static boolean respects(Precedence work, DynamicArray<String> order) {
        HashBackedMap<String, Integer> positionOf = new HashBackedMap<>();
        for (int i = 0; i < order.size(); i++) {
            positionOf.put(order.get(i), Integer.valueOf(i));
        }

        DynamicArray<String> jobs = work.allJobs();
        for (int i = 0; i < jobs.size(); i++) {
            String job = jobs.get(i);
            DynamicArray<String> dependents = work.after(job);
            for (int j = 0; j < dependents.size(); j++) {
                Integer mine = positionOf.get(job);
                Integer theirs = positionOf.get(dependents.get(j));
                if (mine == null || theirs == null) {
                    return false;
                }
                if (mine.intValue() >= theirs.intValue()) {
                    return false;
                }
            }
        }
        return true;
    }
}
