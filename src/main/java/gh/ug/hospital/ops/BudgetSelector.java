package gh.ug.hospital.ops;

import gh.ug.hospital.algo.KnapsackFill;
import gh.ug.hospital.algo.KnapsackReconstruct;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.Parameters;

/**
 * Owner: 11102882
 *
 * Chooses which jobs to do when there are not enough porter-minutes to do
 * them all. Rule BR11.
 *
 * The porters between them have 497 minutes to spend, which is setting P4
 * from our index numbers. Each job costs a certain number of those minutes
 * and is worth a certain amount, based on how urgent it is. We want the most
 * value we can get without going over the budget.
 *
 * This is the knapsack problem. Greedy would take whichever job has the best
 * value per minute and keep going, and greedy gets this wrong: a single
 * awkward job can block a pair of better ones. Dynamic programming builds a
 * table of every budget from 0 up to 497, works out the best answer for each,
 * and reuses those answers as it goes. It cannot be fooled.
 *
 * Two members share this. 11102882 fills the table. 22224236 reads the
 * answer back out of it, because a table alone does not tell you which jobs
 * were chosen, and the brief specifically asks for that step.
 */
public class BudgetSelector {

    /**
     * Picks the best set of jobs within the budget.
     *
     * @param candidates jobs we could do
     */
    public static AlgorithmResult select(DynamicArray<ServiceRequest> candidates) {
        return select(candidates, Parameters.getPorterMinuteBudget());
    }

    public static AlgorithmResult select(DynamicArray<ServiceRequest> candidates,
                                         int budgetMinutes) {
        // The knapsack code works on plain jobs with a cost and a value, so
        // it does not need to know anything about hospitals. Convert first.
        KnapsackFill.Job[] jobs = toJobs(candidates);

        // Step one: fill the table.
        KnapsackFill.run(jobs, budgetMinutes, true);

        // Step two: walk back through the table to find out which jobs were
        // chosen. The table alone gives the best value but not the list.
        AlgorithmResult chosen = KnapsackReconstruct.run(
                KnapsackFill.lastTable(), jobs, budgetMinutes, true);

        checkWithinBudget(chosen, candidates, budgetMinutes);
        return chosen;
    }

    /** Turns service requests into the plain jobs the knapsack code expects. */
    public static KnapsackFill.Job[] toJobs(DynamicArray<ServiceRequest> requests) {
        KnapsackFill.Job[] jobs = new KnapsackFill.Job[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest request = requests.get(i);
            jobs[i] = new KnapsackFill.Job(
                    request.getRequestId(),
                    request.getPorterMinutes(),
                    request.getValue());
        }
        return jobs;
    }

    /** Finds the request behind a chosen job, by its code. */
    public static ServiceRequest requestFor(DynamicArray<ServiceRequest> requests,
                                            String requestId) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getRequestId().equals(requestId)) {
                return requests.get(i);
            }
        }
        return null;
    }

    /**
     * Adds up what the chosen jobs actually cost and complains if it is over.
     *
     * This should never fire. It is here because a quiet mistake in the table
     * would otherwise be invisible, and constraint C11 says the budget is
     * never exceeded.
     */
    @SuppressWarnings("unchecked")
    public static void checkWithinBudget(AlgorithmResult chosen,
                                         DynamicArray<ServiceRequest> candidates,
                                         int budgetMinutes) {
        DynamicArray<ServiceRequest> picked = chosenRequests(chosen, candidates);

        int spent = 0;
        for (int i = 0; i < picked.size(); i++) {
            spent += picked.get(i).getPorterMinutes();
        }

        if (spent > budgetMinutes) {
            throw new DomainException(
                    "The chosen jobs need " + spent + " porter-minutes, but the budget is "
                            + budgetMinutes + ". Something is wrong in the table.");
        }
    }

    /**
     * Turns the knapsack answer back into the hospital requests it stands for.
     */
    @SuppressWarnings("unchecked")
    public static DynamicArray<ServiceRequest> chosenRequests(
            AlgorithmResult chosen, DynamicArray<ServiceRequest> candidates) {

        DynamicArray<ServiceRequest> picked = new DynamicArray<>();
        Object answer = chosen.getAnswer();
        if (!(answer instanceof DynamicArray)) {
            return picked;
        }

        DynamicArray<KnapsackFill.Job> jobs = (DynamicArray<KnapsackFill.Job>) answer;
        for (int i = 0; i < jobs.size(); i++) {
            ServiceRequest request = requestFor(candidates, jobs.get(i).id);
            if (request != null) {
                picked.add(request);
            }
        }
        return picked;
    }

    /** How many minutes a set of jobs uses up. */
    public static int totalCost(DynamicArray<ServiceRequest> jobs) {
        int total = 0;
        for (int i = 0; i < jobs.size(); i++) {
            total += jobs.get(i).getPorterMinutes();
        }
        return total;
    }

    /** How much a set of jobs is worth. */
    public static int totalValue(DynamicArray<ServiceRequest> jobs) {
        int total = 0;
        for (int i = 0; i < jobs.size(); i++) {
            total += jobs.get(i).getValue();
        }
        return total;
    }

    /** A short summary for the console. */
    public static String describe(AlgorithmResult chosen,
                                  DynamicArray<ServiceRequest> candidates,
                                  int budgetMinutes) {
        DynamicArray<ServiceRequest> picked = chosenRequests(chosen, candidates);
        if (picked.size() == 0) {
            return "nothing was chosen";
        }
        return "chose " + picked.size() + " jobs, using "
                + totalCost(picked) + " of " + budgetMinutes + " porter-minutes, "
                + "worth " + totalValue(picked) + " in total";
    }
}
