package gh.ug.hospital.algo;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.exception.DomainException;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owners: 22160537 (greedy), 11102882 (budget table),
 *         22224236 (reading the answer back), 22015166 (referral order)
 *
 * Tests for the four algorithms that make choices rather than just search.
 *
 * The greedy test here is the second of the two counterexamples the brief
 * asks for. It shows a case where taking the best-looking option each time
 * gives a worse total than thinking ahead.
 */
/**
 * Owner: 11102882
 *
 * Tests for the greedy assignment and the budget table.
 */
public class OptimisationTest {

    // ---------------- the budget algorithm ----------------

    @Test
    public void ordinaryUse_theBudgetIsNeverExceeded() {
        KnapsackFill.Job[] jobs = {
                new KnapsackFill.Job("A", 30, 60),
                new KnapsackFill.Job("B", 40, 70),
                new KnapsackFill.Job("C", 50, 90)
        };

        KnapsackFill.run(jobs, 60, false);
        AlgorithmResult chosen = KnapsackReconstruct.run(
                KnapsackFill.lastTable(), jobs, 60, false);

        DynamicArray<KnapsackFill.Job> picked =
                (DynamicArray<KnapsackFill.Job>) chosen.getAnswer();

        int spent = 0;
        for (int i = 0; i < picked.size(); i++) {
            spent += picked.get(i).porterMinutes;
        }
        assertTrue(spent <= 60, "the chosen jobs need " + spent + " minutes");
    }

    @Test
    public void ordinaryUse_thebestValueIsActuallyTheBest() {
        // Budget 50. Taking C alone is worth 90. Taking A and B together
        // costs exactly 50 and is worth 100, so that is the right answer.
        KnapsackFill.Job[] jobs = {
                new KnapsackFill.Job("A", 20, 40),
                new KnapsackFill.Job("B", 30, 60),
                new KnapsackFill.Job("C", 40, 90)
        };

        AlgorithmResult fill = KnapsackFill.run(jobs, 50, false);

        assertEquals(Integer.valueOf(100), fill.getAnswer());
    }

    @Test
    public void edge_readingTheAnswerBackNamesTheJobsNotJustTheTotal() {
        // The table alone gives the best value. It does not say which jobs
        // produced it. The brief asks for both, which is why two members
        // share this: one fills the table, one walks back through it.
        KnapsackFill.Job[] jobs = {
                new KnapsackFill.Job("SMALL", 10, 30),
                new KnapsackFill.Job("BIG", 100, 40)
        };

        KnapsackFill.run(jobs, 20, false);
        AlgorithmResult chosen = KnapsackReconstruct.run(
                KnapsackFill.lastTable(), jobs, 20, false);

        DynamicArray<KnapsackFill.Job> picked =
                (DynamicArray<KnapsackFill.Job>) chosen.getAnswer();

        assertEquals(1, picked.size());
        assertEquals("SMALL", picked.get(0).id);
    }

    @Test
    public void badInput_anImpossibleBudgetIsRefused() {
        final KnapsackFill.Job[] jobs = {
            new KnapsackFill.Job("Q001", 10, 30)
        };

        // A negative budget cannot be filled in, and quietly returning an
        // empty answer would look like a correct result.
        assertThrows(DomainException.class, new Executable() {
            public void execute() {
                KnapsackFill.run(jobs, -1, false);
            }
        });

        // A job cannot cost negative minutes either.
        assertThrows(IllegalArgumentException.class, new Executable() {
            public void execute() {
                new KnapsackFill.Job("Q002", -5, 10);
            }
        });
    }
}
