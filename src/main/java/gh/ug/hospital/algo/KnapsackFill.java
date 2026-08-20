package gh.ug.hospital.algo;

import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.Parameters;

/**
 * Owner: 11102882
 *
 * Works out the most useful set of jobs that fits inside the day's budget.
 *
 * The situation. There are more supply runs, linen moves and deferrable
 * transfers than the porters have time for. Each job costs a number of
 * porter-minutes and is worth a certain amount, based on how urgent it is.
 * The budget is 497 porter-minutes, which came from our index numbers as
 * setting P4. Which jobs should be done?
 *
 * Greedy would sort by value per minute and take from the top. That is often
 * good and sometimes wrong, because it cannot back out of an early choice.
 * Dynamic programming gets the exact best answer by working out, for every
 * budget from 0 up to 497 and every number of jobs from 0 upward, what the
 * best value is. Each answer builds on smaller ones that are already worked
 * out, so nothing is calculated twice.
 *
 * This class fills the table. KnapsackReconstruct then reads back through it
 * to say which jobs were actually chosen, because the table alone gives the
 * best value but not the list. The brief asks for both.
 *
 * Why the answer is right. Let best[i][b] be the most value using only the
 * first i jobs within budget b. For job i there are two possibilities and no
 * others: leave it out, giving best[i-1][b], or take it, giving its value
 * plus best[i-1][b - its cost]. We take whichever is larger, so best[i][b] is
 * correct whenever the row above is correct. Row 0 is correct because with no
 * jobs the value is 0. So every row is correct, and best[n][497] is the
 * answer.
 *
 * Speed: O(jobs x budget). Not linear, but the budget is a fixed 497.
 */
public class KnapsackFill {

    /** One job that could be done today. */
    public static class Job {
        public final String id;
        public final int porterMinutes;
        public final int value;

        public Job(String id, int porterMinutes, int value) {
            if (porterMinutes < 0) {
                throw new IllegalArgumentException(
                        "A job cannot cost negative minutes: " + id);
            }
            this.id = id;
            this.porterMinutes = porterMinutes;
            this.value = value;
        }

        @Override
        public String toString() {
            return id + "(" + porterMinutes + " min, worth " + value + ")";
        }
    }

    /** Uses the budget from setting P4. */
    public static AlgorithmResult run(Job[] jobs) {
        return run(jobs, Parameters.getPorterMinuteBudget(), false);
    }

    public static AlgorithmResult run(Job[] jobs, int budgetMinutes, boolean withTrace) {
        if (budgetMinutes < 0) {
            throw new DomainException(
                    "The budget cannot be negative, but got " + budgetMinutes);
        }

        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        int jobCount = jobs.length;

        // best[i][b] = most value using the first i jobs within budget b.
        int[][] best = new int[jobCount + 1][budgetMinutes + 1];

        // Row 0 stays all zeros: no jobs means no value, whatever the budget.

        for (int i = 1; i <= jobCount; i++) {
            Job job = jobs[i - 1];

            for (int budget = 0; budget <= budgetMinutes; budget++) {
                result.countComparison();

                // Choice one: skip this job.
                int skipIt = best[i - 1][budget];

                // Choice two: take it, if it fits.
                int takeIt = 0;
                if (job.porterMinutes <= budget) {
                    takeIt = job.value + best[i - 1][budget - job.porterMinutes];
                }

                if (takeIt > skipIt) {
                    best[i][budget] = takeIt;
                    result.countSwap();
                } else {
                    best[i][budget] = skipIt;
                }
            }

            if (withTrace) {
                String[] extras = {
                        job.id,
                        String.valueOf(budgetMinutes),
                        String.valueOf(best[i][budgetMinutes]),
                        "-"
                };
                result.addTrace("finish row for " + job.id,
                        "best value so far " + best[i][budgetMinutes],
                        "using the first " + i + " job(s)",
                        extras);
            }
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(Integer.valueOf(best[jobCount][budgetMinutes]));

        lastTable = best;
        lastJobs = jobs;
        lastBudget = budgetMinutes;
        return result;
    }

    private static int[][] lastTable;
    private static Job[] lastJobs;
    private static int lastBudget;

    /** The filled table, which KnapsackReconstruct reads back through. */
    public static int[][] lastTable() {
        return lastTable;
    }

    public static Job[] lastJobs() {
        return lastJobs;
    }

    public static int lastBudget() {
        return lastBudget;
    }

    public static String[] traceColumns() {
        return new String[]{"Job", "Budget", "Table Value", "Chosen?"};
    }

    /**
     * The greedy answer to the same problem: sort by value per minute and
     * take from the top while it fits.
     *
     * This exists so the report can put the two side by side. On most data
     * they agree. On the case in the report they do not, and the difference
     * is the counterexample.
     */
    public static int greedyTotal(Job[] jobs, int budgetMinutes) {
        Job[] order = new Job[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            order[i] = jobs[i];
        }

        // Insertion sort by value per minute, best first.
        for (int i = 1; i < order.length; i++) {
            Job key = order[i];
            double keyRatio = ratio(key);
            int j = i - 1;
            while (j >= 0 && ratio(order[j]) < keyRatio) {
                order[j + 1] = order[j];
                j--;
            }
            order[j + 1] = key;
        }

        int spent = 0;
        int value = 0;
        for (int i = 0; i < order.length; i++) {
            if (spent + order[i].porterMinutes <= budgetMinutes) {
                spent += order[i].porterMinutes;
                value += order[i].value;
            }
        }
        return value;
    }

    private static double ratio(Job job) {
        if (job.porterMinutes == 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) job.value / (double) job.porterMinutes;
    }
}
