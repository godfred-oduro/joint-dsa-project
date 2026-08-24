package gh.ug.hospital.app;

import gh.ug.hospital.algo.BinarySearch;
import gh.ug.hospital.algo.BreadthFirstSearch;
import gh.ug.hospital.algo.DepthFirstSearch;
import gh.ug.hospital.algo.Dijkstra;
import gh.ug.hospital.algo.InsertionSort;
import gh.ug.hospital.algo.Kruskal;
import gh.ug.hospital.algo.LinearSearch;
import gh.ug.hospital.algo.MergeSort;
import gh.ug.hospital.algo.Prim;
import gh.ug.hospital.algo.QuickSort;
import gh.ug.hospital.algo.SelectionSort;
import gh.ug.hospital.ops.ReferralOrdering;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.TablePrinter;

import java.util.Comparator;

/**
 * Owner: 22248029
 *
 * Menu option 3. Runs each of the 17 algorithms on its own.
 *
 * The numbering is fixed and matches the project guide. M3.11 is always
 * Dijkstra, whatever else changes.
 *
 * Each run prints the answer, how much work it did, and how long it took.
 * The trace tables live under menu option 6 instead, so that this menu stays
 * quick to page through.
 */
public class AlgorithmMenu {

    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    private static final String[] OPTIONS = {
            "Linear search            (22305896)",
            "Binary search            (22392509)",
            "Selection sort           (22372680)",
            "Insertion sort           (22370298)",
            "Merge sort               (22367946)",
            "Quick sort               (22395395)",
            "Priority order by colour (22399601)",
            "Brute-force route list   (22399010)",
            "Breadth first search     (22372639)",
            "Depth first search       (22372639)",
            "Dijkstra shortest route  (22248029)",
            "Prim spanning tree       (22104879)",
            "Kruskal spanning tree    (22040255)",
            "Topological sort         (22015166)",
            "Greedy assignment        (22160537)",
            "Budget table fill        (11102882)",
            "Budget answer read back  (22224236)"
    };

    private final Session session;

    public AlgorithmMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            Console.heading("3. ALGORITHMS");
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Run which", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }
            runOne(choice);
            Console.pause();
        }
    }

    private void runOne(int choice) {
        Console.heading("3." + choice + "  " + OPTIONS[choice - 1]);

        // Options 8 to 17 all need the hospital map or the loaded requests.
        if (choice >= 8 && !session.requireLoaded()) {
            return;
        }

        if (choice == 1) {
            Integer[] data = countUp(1000);
            report(LinearSearch.run(data, Integer.valueOf(999), BY_VALUE),
                    "Looked for the last of 1000 items, which is the worst case.");

        } else if (choice == 2) {
            Integer[] data = countUp(1000);
            report(BinarySearch.run(data, Integer.valueOf(999), BY_VALUE),
                    "Same search, but halving the list each time. Compare the "
                            + "comparison counts.");

        } else if (choice == 3) {
            report(SelectionSort.run(shuffled(200), BY_VALUE, true),
                    "Always makes the same number of comparisons, whatever the data.");

        } else if (choice == 4) {
            report(InsertionSort.run(shuffled(200), BY_VALUE, true),
                    "Fast on data that is nearly sorted already.");

        } else if (choice == 5) {
            report(MergeSort.run(shuffled(200), BY_VALUE, true),
                    "Splits, sorts the halves, merges them back.");

        } else if (choice == 6) {
            report(QuickSort.run(shuffled(200), BY_VALUE, true),
                    "Picks a pivot and partitions around it.");

        } else if (choice == 7) {
            Console.say("Triage priority ordering works on the five colours, using a");
            Console.say("Comparator-driven binary heap rather than counting sort - it");
            Console.say("does make real comparisons, they are just cheap because there");
            Console.say("are only five colours to compare against.");
            Console.say("Run it from menu option 4.1, where the requests are loaded.");

        } else if (choice == 8) {
            Console.say("Listing every route from the main gate to the children's ward,");
            Console.say("up to 6 steps long. This is the slow way on purpose.");
            report(gh.ug.hospital.algo.BruteForceRoutes.run(
                            session.getListMap(), "L001", "L004", 6),
                    "Brute force checks everything. Compare with Dijkstra at 3.11.");

        } else if (choice == 9) {
            report(BreadthFirstSearch.run(session.getListMap(), "L001"),
                    "Visits everything by how many steps away it is.");

        } else if (choice == 10) {
            report(DepthFirstSearch.run(session.getListMap(), "L001"),
                    "Follows one path as far as it goes before backing up.");

        } else if (choice == 11) {
            AlgorithmResult result = Dijkstra.run(session.getListMap(), "L001", "L004", true);
            report(result, "Route: " + result.getAnswer());
            Console.say(String.format("Total time: %.2f minutes",
                    Dijkstra.lastTotalMinutes()));

        } else if (choice == 12) {
            AlgorithmResult result = Prim.run(session.getListMap(), "L001", true);
            report(result, String.format(
                    "Cheapest set of paths joining everything: %.2f minutes total.",
                    Prim.lastTotalMinutes()));

        } else if (choice == 13) {
            AlgorithmResult result = Kruskal.run(session.getListMap(), true);
            report(result, String.format(
                    "Same job, different method: %.2f minutes total, %d groups left.",
                    Kruskal.lastTotalMinutes(), Kruskal.lastGroupCount()));

        } else if (choice == 14) {
            AlgorithmResult result = ReferralOrdering.order(session.getRequests());
            report(result, "An order that respects every 'see the GP first' rule.");
            Console.say(ReferralOrdering.summarise(session.getRequests()));

        } else if (choice == 15) {
            Console.say("Greedy assignment is easiest to see with real jobs and real");
            Console.say("porters. Run it from menu option 4.2.");

        } else if (choice == 16 || choice == 17) {
            Console.say("The budget table and reading the answer back out of it work");
            Console.say("together. Run them from menu option 4.5.");
        }
    }

    /** Prints an algorithm's answer and how much work it took. */
    private void report(AlgorithmResult result, String note) {
        Console.blank();
        Console.say(note);
        Console.blank();
        Console.say("  " + result.summary());

        if (result.getTraceCount() > 0) {
            Console.blank();
            Console.say("First few steps:");
            TablePrinter.printTrace(result, null);
        }
    }

    private Integer[] countUp(int howMany) {
        Integer[] numbers = new Integer[howMany];
        for (int i = 0; i < howMany; i++) {
            numbers[i] = Integer.valueOf(i);
        }
        return numbers;
    }

    /** Mixed-up numbers, the same every run so results can be repeated. */
    private Integer[] shuffled(int howMany) {
        Integer[] numbers = countUp(howMany);
        java.util.Random dice =
                new java.util.Random(gh.ug.hospital.util.Parameters.getRandomSeed());
        for (int i = howMany - 1; i > 0; i--) {
            int j = dice.nextInt(i + 1);
            Integer hold = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = hold;
        }
        return numbers;
    }
}
