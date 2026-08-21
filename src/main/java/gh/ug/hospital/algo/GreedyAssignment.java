package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22160537
 *
 * Gives each job to the nearest free porter, one job at a time.
 *
 * This is the greedy method: at every step take the choice that looks best
 * right now and never look back. Deal with the most urgent job first, hand it
 * to whichever suitable resource can get there quickest, mark that resource
 * busy, move on.
 *
 * It is fast and it is usually good. It is not always best, and the report
 * has to show a case where it fails. The counterexample below does that.
 *
 * Why it can fail. Suppose porter A is 1 minute from job 1 and 2 minutes from
 * job 2, while porter B is 9 minutes from job 1 and cannot reach job 2 at
 * all. Greedy handles job 1 first, sees that A is nearest, and sends A. Job 2
 * then has nobody, because B cannot do it. Total: one job unassigned. The
 * better answer was to send B to job 1 and A to job 2, finishing both.
 *
 * Greedy cannot see that, because it never looks at job 2 while deciding
 * job 1. That is not a bug. It is what greedy means.
 *
 * Speed: O(jobs x resources).
 */
public class GreedyAssignment {

    /** A job waiting to be done. */
    public static class Job {
        public final String id;
        public final String atPlace;
        public final int urgency;       // 5 is most urgent
        public final String needsType;  // PORTER, AMBULANCE, and so on

        public Job(String id, String atPlace, int urgency, String needsType) {
            this.id = id;
            this.atPlace = atPlace;
            this.urgency = urgency;
            this.needsType = needsType;
        }

        @Override
        public String toString() {
            return id + "@" + atPlace + " (urgency " + urgency + ")";
        }
    }

    /** Someone or something that can do a job. */
    public static class Worker {
        public final String id;
        public final String basedAt;
        public final String type;
        public final int capacity;

        public Worker(String id, String basedAt, String type, int capacity) {
            this.id = id;
            this.basedAt = basedAt;
            this.type = type;
            this.capacity = capacity;
        }

        @Override
        public String toString() {
            return id + "@" + basedAt + " (" + type + ")";
        }
    }

    /** One decision: this job goes to this worker, and takes this long. */
    public static class Assignment {
        public final Job job;
        public final Worker worker;
        public final double minutes;

        Assignment(Job job, Worker worker, double minutes) {
            this.job = job;
            this.worker = worker;
            this.minutes = minutes;
        }

        @Override
        public String toString() {
            return job.id + " -> " + worker.id
                    + " (" + String.format("%.2f", minutes) + " min)";
        }
    }

    public static AlgorithmResult run(GraphView graph, Job[] jobs, Worker[] workers) {
        return run(graph, jobs, workers, false);
    }

    public static AlgorithmResult run(GraphView graph, Job[] jobs, Worker[] workers,
                                      boolean withTrace) {
        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        // Most urgent job first. Counting sort does this in one pass.
        Job[] order = new Job[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            order[i] = jobs[i];
        }
        sortMostUrgentFirst(order, result);

        HashBackedMap<String, Integer> jobsTaken = new HashBackedMap<>();
        for (int i = 0; i < workers.length; i++) {
            jobsTaken.put(workers[i].id, Integer.valueOf(0));
        }

        DynamicArray<Assignment> decisions = new DynamicArray<>();
        DynamicArray<Job> unassigned = new DynamicArray<>();
        double totalMinutes = 0.0;

        for (int j = 0; j < order.length; j++) {
            Job job = order[j];

            Worker best = null;
            double bestMinutes = Double.POSITIVE_INFINITY;

            for (int w = 0; w < workers.length; w++) {
                Worker worker = workers[w];
                result.countComparison();

                if (!worker.type.equals(job.needsType)) {
                    continue;   // wrong kind of worker
                }
                if (jobsTaken.get(worker.id).intValue() >= worker.capacity) {
                    continue;   // already at capacity
                }
                if (!graph.hasVertex(worker.basedAt) || !graph.hasVertex(job.atPlace)) {
                    continue;
                }

                Dijkstra.run(graph, worker.basedAt, job.atPlace);
                double minutes = Dijkstra.lastTotalMinutes();

                if (minutes < bestMinutes) {
                    bestMinutes = minutes;
                    best = worker;
                }
            }

            if (best == null || bestMinutes == Double.POSITIVE_INFINITY) {
                unassigned.add(job);
                if (withTrace) {
                    result.addTrace("job " + job.id, "nobody available",
                            "no free " + job.needsType + " can reach " + job.atPlace);
                }
                continue;
            }

            jobsTaken.put(best.id, Integer.valueOf(jobsTaken.get(best.id).intValue() + 1));
            decisions.add(new Assignment(job, best, bestMinutes));
            totalMinutes += bestMinutes;
            result.countSwap();

            if (withTrace) {
                result.addTrace("job " + job.id + " (urgency " + job.urgency + ")",
                        best.id + " sent from " + best.basedAt,
                        "nearest free worker, "
                                + String.format("%.2f", bestMinutes) + " min away");
            }
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(decisions);
        lastTotalMinutes = totalMinutes;
        lastUnassigned = unassigned;
        return result;
    }

    /** Simple insertion sort by urgency, most urgent first. */
    private static void sortMostUrgentFirst(Job[] jobs, AlgorithmResult result) {
        for (int i = 1; i < jobs.length; i++) {
            Job key = jobs[i];
            int j = i - 1;
            while (j >= 0 && jobs[j].urgency < key.urgency) {
                result.countComparison();
                jobs[j + 1] = jobs[j];
                result.countSwap();
                j--;
            }
            jobs[j + 1] = key;
        }
    }

    private static double lastTotalMinutes;
    private static DynamicArray<Job> lastUnassigned;

    public static double lastTotalMinutes() {
        return lastTotalMinutes;
    }

    /** Jobs nobody could take. Greedy leaves more of these than it needs to. */
    public static DynamicArray<Job> lastUnassigned() {
        return lastUnassigned;
    }

    /**
     * Tries every possible assignment and keeps the best total.
     *
     * Only usable on tiny examples, because the number of possibilities grows
     * enormously. Its job here is to prove the counterexample: on the small
     * case in the report, this finds an answer greedy misses.
     */
    public static double bestPossibleTotal(GraphView graph, Job[] jobs,
                                           Worker[] workers) {
        return tryEveryWay(graph, jobs, workers, 0, new HashBackedSet<String>(), 0.0);
    }

    private static double tryEveryWay(GraphView graph, Job[] jobs, Worker[] workers,
                                      int jobIndex, HashBackedSet<String> used,
                                      double runningTotal) {
        if (jobIndex >= jobs.length) {
            return runningTotal;
        }

        double best = Double.POSITIVE_INFINITY;
        Job job = jobs[jobIndex];

        for (int w = 0; w < workers.length; w++) {
            Worker worker = workers[w];
            if (used.contains(worker.id)) {
                continue;
            }
            if (!worker.type.equals(job.needsType)) {
                continue;
            }
            if (!graph.hasVertex(worker.basedAt) || !graph.hasVertex(job.atPlace)) {
                continue;
            }

            Dijkstra.run(graph, worker.basedAt, job.atPlace);
            double minutes = Dijkstra.lastTotalMinutes();
            if (minutes == Double.POSITIVE_INFINITY) {
                continue;
            }

            used.add(worker.id);
            double whole = tryEveryWay(graph, jobs, workers, jobIndex + 1, used,
                    runningTotal + minutes);
            used.remove(worker.id);

            if (whole < best) {
                best = whole;
            }
        }
        return best;
    }
}
