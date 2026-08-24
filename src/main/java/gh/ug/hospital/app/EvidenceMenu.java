package gh.ug.hospital.app;

import gh.ug.hospital.algo.BinarySearch;
import gh.ug.hospital.algo.GreedyAssignment;
import gh.ug.hospital.ds.ChainedHashTable;
import gh.ug.hospital.ds.graph.AdjacencyListGraph;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.TablePrinter;

import java.util.Comparator;

/**
 * Owner: 22399010
 *
 * Menu option M6. Prints the evidence the report needs.
 *
 * Every trace table in the report must be reproducible from this menu, and
 * every caption names the option that produced it. That way an examiner can
 * check any table in the report by running one menu choice.
 *
 * Menu numbers M6.1 to M6.10 match the guide, so they must not be shuffled.
 */
public class EvidenceMenu {

    private final Session session;

    /** Compares numbers the ordinary way. Used by the counterexamples. */
    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    public EvidenceMenu(Session session) {
        this.session = session;
    }

    public void show() {
        boolean stay = true;

        while (stay) {
            Console.heading("M6  EVIDENCE");
            Console.say("   1  Binary search trace              owner 22392509");
            Console.say("   2  Insertion sort trace             owner 22370298");
            Console.say("   3  Merge or quick sort trace        owner 22367946 / 22395395");
            Console.say("   4  Dijkstra trace                   owner 22248029");
            Console.say("   5  Kruskal or Prim trace            owner 22040255 / 22104879");
            Console.say("   6  Budget table and read-back       owner 11102882 / 22224236");
            Console.say("   7  Collisions by table fullness     owner 22370298 + 22015166");
            Console.say("   8  Where greedy fails               owner 22160537");
            Console.say("   9  Binary search on unsorted data   owner 22392509");
            Console.say("  10  Structure evidence               each owner");
            Console.say("   0  Back");

            int choice = Console.askNumber("Choose", 0, 10);

            if (choice == 0) {
                stay = false;
            } else if (choice == 7) {
                showCollisionEvidence();
            } else if (choice == 8) {
                showGreedyCounterexample();
            } else if (choice == 9) {
                showUnsortedCounterexample();
            } else if (choice == 10) {
                Console.say("Use menu M2 and pick a structure. Each one prints its own");
                Console.say("evidence: the array growing, the queue wrapping round, the");
                Console.say("tree rotating, the B-tree node splitting.");
                Console.pause();
            } else {
                Console.say("Run the algorithm from menu M3 first, then come back here.");
                Console.say("The trace is taken from the run, so there has to be a run.");
                Console.pause();
            }
        }
    }

    /**
     * M6.7. Fills both hash tables to the same fullness and shows how
     * differently they cope.
     *
     * This is the evidence for experiment E3, and it is why the two hash
     * table owners were paired together.
     */
    private void showCollisionEvidence() {
        Console.heading("M6.7  COLLISIONS AS THE TABLE FILLS UP");
        Console.say("A hash table turns a key into a slot number. Two different");
        Console.say("keys can land on the same slot, which is a collision. This");
        Console.say("table handles it by hanging a small chain off each slot.");
        Console.blank();
        Console.say("As the table fills, more keys collide, the chains grow, and");
        Console.say("every lookup has further to walk. That is what this shows.");
        Console.blank();

        int slots = 1009;   // a prime, which spreads keys more evenly
        double[] fullnessLevels = {0.3, 0.5, 0.7, 0.9};

        String[] headers = {"Fullness", "Keys", "Collisions",
                "Longest chain", "Average chain"};
        String[][] rows = new String[fullnessLevels.length][];

        for (int level = 0; level < fullnessLevels.length; level++) {
            int keyCount = (int) (slots * fullnessLevels[level]);

            ChainedHashTable<String, Integer> table = new ChainedHashTable<>(slots);
            for (int k = 0; k < keyCount; k++) {
                table.put("L" + k, Integer.valueOf(k));
            }

            rows[level] = new String[]{
                    String.format("%.1f", fullnessLevels[level]),
                    String.valueOf(keyCount),
                    String.valueOf(table.getCollisions()),
                    String.valueOf(table.longestChain()),
                    String.format("%.2f", table.averageChainLength())
            };
        }

        TablePrinter.print(headers, rows);
        Console.blank();
        Console.say("What to notice: the collisions climb steadily rather than");
        Console.say("suddenly. A longer chain is still just a longer walk, so a");
        Console.say("chained table gets slower gently instead of falling over.");
        Console.pause();
    }
    /**
     * M6.8. The first required counterexample: greedy is not always best.
     *
     * We build a tiny map on purpose, small enough that the best possible
     * answer can be worked out by trying every combination. Then we let
     * greedy loose on it and compare.
     */
    private void showGreedyCounterexample() {
        Console.heading("M6.8  WHERE GREEDY FAILS");
        Console.say("Greedy assignment always picks the nearest free porter for the");
        Console.say("job in front of it. That is fast and usually fine. It is not");
        Console.say("always best, and here is a case where it is not.");
        Console.blank();

        // This is the exact scenario described in GreedyAssignment's own
        // class comment: porter A is close to both jobs, porter B is far
        // from job 1 and cannot reach job 2 at all. Greedy deals with the
        // more urgent job first, sees A is nearest, and sends A there --
        // leaving job 2 with nobody, because B cannot reach it. The better
        // answer sends B to job 1 (slow but it gets there) and saves A for
        // the job only A can reach.
        //
        // The earlier version of this method used a symmetric map where
        // ties happened to resolve to the optimal answer on every run,
        // so greedy and optimal always matched and no gap ever appeared.
        // This version removes B's path to job 2 entirely, so the failure
        // is structural and shows up every time, not just on some runs.
        AdjacencyListGraph map = new AdjacencyListGraph();
        map.addVertex("A");
        map.addVertex("B");
        map.addVertex("J1");
        map.addVertex("J2");
        map.addEdge("A", "J1", 1.0);
        map.addEdge("A", "J2", 2.0);
        map.addEdge("B", "J1", 9.0);
        // Deliberately no edge from B to J2: B cannot get there at all.

        Console.say("The map:   B --9-- J1 --1-- A --2-- J2");
        Console.say("           (B has no path to J2 at all)");
        Console.say("Two jobs:  one at J1, one at J2. Both need a porter.");
        Console.say("Two porters: one at A, one at B.");
        Console.blank();

        GreedyAssignment.Job[] jobs = {
                new GreedyAssignment.Job("JOB-J1", "J1", 5, "PORTER"),
                new GreedyAssignment.Job("JOB-J2", "J2", 3, "PORTER")
        };
        GreedyAssignment.Worker[] workers = {
                new GreedyAssignment.Worker("P-at-A", "A", "PORTER", 1),
                new GreedyAssignment.Worker("P-at-B", "B", "PORTER", 1)
        };

        AlgorithmResult greedy = GreedyAssignment.run(map, jobs, workers, true);
        TablePrinter.printTrace(greedy, null);

        double greedyTotal = GreedyAssignment.lastTotalMinutes();
        double bestTotal = GreedyAssignment.bestPossibleTotal(map, jobs, workers);

        Console.blank();
        Console.say(String.format("Greedy total:        %.1f minutes", greedyTotal));
        Console.say(String.format("Best possible total: %.1f minutes", bestTotal));
        Console.blank();

        int unassignedCount = GreedyAssignment.lastUnassigned().size();

        // A raw total-minutes comparison is not the right test here: leaving
        // a job unassigned makes the total go DOWN, since fewer jobs got
        // served, which would make greedy look like a win if we only
        // compared totals. The real failure signal is whether greedy left
        // anyone stranded that the optimal plan managed to serve.
        if (unassignedCount > 0) {
            Console.say("Greedy loses. It gave the closer job, JOB-J1, to the porter");
            Console.say("at A, because A is one minute away and that looks best right");
            Console.say("now. That leaves the porter at B for JOB-J2 -- but B has no");
            Console.say("path there at all, so JOB-J2 goes undone.");
            Console.blank();
            Console.say("Sending B to J1 instead (nine minutes, slower but reachable)");
            Console.say("would have freed A to reach J2 (two minutes), finishing both");
            Console.say("jobs for 11.0 minutes total. Greedy cannot see that, because");
            Console.say("it never looks at job 2 while deciding job 1. That is not a");
            Console.say("bug. It is what greedy means.");
        } else if (greedyTotal > bestTotal) {
            Console.say("Greedy loses. It took a locally-nearest choice on an earlier");
            Console.say("job that left a later job more expensive than it needed to be.");
        } else {
            Console.say("On this run greedy happened to match the best answer.");
            Console.say("Try changing the path costs above to make the gap appear.");
        }
        Console.pause();
    }

    /**
     * M6.9. The second required counterexample: binary search on unsorted
     * data does not go slow, it goes wrong.
     */
    private void showUnsortedCounterexample() {
        Console.heading("M6.9  BINARY SEARCH ON UNSORTED DATA");
        Console.say("Binary search works by throwing away half the data at every");
        Console.say("step. That only makes sense if the data is sorted. On unsorted");
        Console.say("data it may throw away the half holding the answer.");
        Console.blank();

        // The number 7 is plainly in this array, at position 4.
        Integer[] unsorted = {
                Integer.valueOf(50), Integer.valueOf(20), Integer.valueOf(90),
                Integer.valueOf(10), Integer.valueOf(7), Integer.valueOf(60),
                Integer.valueOf(30)
        };
        Integer target = Integer.valueOf(7);

        Console.say("The data:   [50, 20, 90, 10, 7, 60, 30]");
        Console.say("Looking for: 7, which is sitting at position 4.");
        Console.blank();

        AlgorithmResult result =
                BinarySearch.searchWithoutChecking(unsorted, target, BY_VALUE);

        TablePrinter.printTrace(result, BinarySearch.traceExtraHeaders());

        Console.blank();
        Console.say("Answer returned: " + result.getAnswer()
                + "   (-1 means not found)");
        Console.blank();
        Console.say("The item is there. Binary search says it is not. It looked at");
        Console.say("the middle, saw 10, decided 7 must be to the left, and threw");
        Console.say("away the half that actually held it.");
        Console.blank();
        Console.say("This is why BinarySearch.run checks the data first and throws");
        Console.say("DomainException. The version used here skips that check");
        Console.say("on purpose, so the failure can be shown.");
        Console.pause();
    }
}
