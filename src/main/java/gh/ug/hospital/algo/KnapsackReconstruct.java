package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.util.AlgorithmResult;

/**
 * Owner: 22224236
 *
 * Reads back through the filled table to say which jobs were chosen.
 *
 * KnapsackFill produces the best possible value, a single number. That is not
 * enough. A porter cannot act on "the best you can do today is worth 47".
 * They need the list of jobs.
 *
 * The table holds enough to recover it. Start at the bottom right corner,
 * which is the full set of jobs and the full budget. Ask one question at each
 * step: is the value here the same as the value in the row above? If yes,
 * this job was not taken, so move up. If no, the only reason the value could
 * have gone up is that this job was taken, so record it, subtract its cost
 * from the budget, and move up.
 *
 * Keep going until the top row. What we recorded is the chosen set.
 *
 * The brief asks for the reconstruction specifically, because a project that
 * fills the table and stops has done only half the work.
 *
 * Speed: O(jobs). One step per row, going up.
 */
public class KnapsackReconstruct {

    /** Uses whatever KnapsackFill worked out last. */
    public static AlgorithmResult run() {
        return run(KnapsackFill.lastTable(), KnapsackFill.lastJobs(),
                KnapsackFill.lastBudget(), false);
    }

    public static AlgorithmResult run(int[][] table, KnapsackFill.Job[] jobs,
                                      int budgetMinutes, boolean withTrace) {
        if (table == null || jobs == null) {
            throw new IllegalStateException(
                    "Fill the table first. Run KnapsackFill before this.");
        }

        AlgorithmResult result = new AlgorithmResult();
        long startTime = System.nanoTime();

        DynamicArray<KnapsackFill.Job> chosen = new DynamicArray<>();
        int budgetLeft = budgetMinutes;
        int totalValue = 0;
        int totalMinutes = 0;

        // Walk up from the last row to the first.
        for (int i = jobs.length; i > 0; i--) {
            KnapsackFill.Job job = jobs[i - 1];
            result.countComparison();

            if (table[i][budgetLeft] == table[i - 1][budgetLeft]) {
                // Same value without this row, so the job was not taken.
                if (withTrace) {
                    String[] extras = {
                            job.id,
                            String.valueOf(budgetLeft),
                            String.valueOf(table[i][budgetLeft]),
                            "no"
                    };
                    result.addTrace("check " + job.id,
                            "budget left " + budgetLeft,
                            "value did not change, so this job was skipped",
                            extras);
                }
                continue;
            }

            // The value went up, so this job must have been taken.
            chosen.add(job);
            totalValue += job.value;
            totalMinutes += job.porterMinutes;
            budgetLeft -= job.porterMinutes;
            result.countSwap();

            if (budgetLeft < 0) {
                throw new DomainException(
                        "Reading back through the table spent more than the budget. "
                                + "The table is wrong.");
            }

            if (withTrace) {
                String[] extras = {
                        job.id,
                        String.valueOf(budgetLeft),
                        String.valueOf(table[i][budgetLeft + job.porterMinutes]),
                        "yes"
                };
                result.addTrace("check " + job.id,
                        "budget left " + budgetLeft,
                        "value rose, so this job was taken",
                        extras);
            }
        }

        // The list came out backwards, so flip it into job order.
        DynamicArray<KnapsackFill.Job> inOrder = new DynamicArray<>();
        for (int i = chosen.size() - 1; i >= 0; i--) {
            inOrder.add(chosen.get(i));
        }

        result.setElapsedNanos(System.nanoTime() - startTime);
        result.setAnswer(inOrder);

        lastTotalValue = totalValue;
        lastTotalMinutes = totalMinutes;
        lastBudget = budgetMinutes;
        return result;
    }

    private static int lastTotalValue;
    private static int lastTotalMinutes;
    private static int lastBudget;

    public static int lastTotalValue() {
        return lastTotalValue;
    }

    public static int lastTotalMinutes() {
        return lastTotalMinutes;
    }

    /**
     * Checks the chosen set really does fit the budget. Constraint C11 says
     * it must, and the test suite asserts this.
     */
    public static boolean lastRunFitsBudget() {
        return lastTotalMinutes <= lastBudget;
    }

    public static String[] traceColumns() {
        return new String[]{"Job", "Budget Left", "Table Value", "Chosen?"};
    }
}
