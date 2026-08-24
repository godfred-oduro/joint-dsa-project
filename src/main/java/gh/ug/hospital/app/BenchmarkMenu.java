package gh.ug.hospital.app;

import java.sql.SQLException;

import gh.ug.hospital.bench.CsvExporter;
import gh.ug.hospital.bench.ExperimentRunner;
import gh.ug.hospital.bench.TimingHarness;
import gh.ug.hospital.db.AlgorithmRunDao;

/**
 * Owner: 22224236
 *
 * Menu option 5. Runs the six timing experiments and saves the results.
 *
 * A warning before anyone runs all six: it takes several minutes, mostly
 * because the sorting experiment does ten thousand items four times over for
 * four different sorts. That is not a bug.
 *
 * Every measurement is taken four times and the first is thrown away, because
 * Java speeds up as it runs. The results go into two files: one with every
 * run so the working can be checked, and one with the averages we plot.
 */
public class BenchmarkMenu {

    private static final String[] OPTIONS = {
            "E1  linear search against binary search",
            "E2  the four sorting methods compared",
            "E3  hash table fullness against collisions",
            "E4  plain search tree against balanced tree",
            "E5  heap insert and remove",
            "E6  the four graph algorithms",
            "Run all six (takes several minutes)",
            "Save the results to files",
            "Save the results to the database"
    };

    private final Session session;
    private final TimingHarness harness = new TimingHarness();
    private final ExperimentRunner runner = new ExperimentRunner(harness);
    private final AlgorithmRunDao runDao = new AlgorithmRunDao();

    public BenchmarkMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            Console.heading("5. TIMING EXPERIMENTS");
            Console.say("  Measurements taken so far: " + harness.runCount());
            Console.say("  Computer: Dell Latitude 7480, i5-6300U, 16GB, JDK 21");
            Console.blank();
            Console.say("  Start Java with -Xms2g -Xmx2g or the numbers will wander.");
            Console.blank();
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Choose", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }
            handle(choice);
            Console.pause();
        }
    }

    private void handle(int choice) {
        if (choice >= 1 && choice <= 6) {
            runOne(choice);
        } else if (choice == 7) {
            Console.say("Running all six. This will take several minutes.");
            Console.say("Close other programs and leave it alone while it works.");
            Console.blank();
            for (int i = 1; i <= 6; i++) {
                runOne(i);
            }
        } else if (choice == 8) {
            saveToFiles();
        } else if (choice == 9) {
            saveToDatabase();
        }
    }

    /**
     * The algorithm names measured by each experiment, in the same order
     * every time. Used both for the on-screen report and for the averaged
     * CSV files, so the two can never drift apart.
     */
    private String[] algorithmNamesFor(int number) {
        switch (number) {
            case 1: return new String[]{"LinearSearch", "BinarySearch"};
            case 2: return new String[]{"SelectionSort", "InsertionSort", "MergeSort", "QuickSort"};
            case 3: return new String[]{"ChainedHashTable"};
            case 4: return new String[]{"BST-shuffled", "BST-sorted", "RedBlackTree-sorted"};
            case 5: return new String[]{"HeapInsert", "HeapExtract"};
            case 6: return new String[]{"BreadthFirstSearch", "DepthFirstSearch", "Dijkstra", "Kruskal"};
            default: return new String[0];
        }
    }

    private int[] sizesFor(int number) {
        switch (number) {
            case 1: return ExperimentRunner.E1_SIZES;
            case 2: return ExperimentRunner.E2_SIZES;
            case 3: return ExperimentRunner.E3_SIZES;
            case 4: return ExperimentRunner.E4_SIZES;
            case 5: return ExperimentRunner.E5_SIZES;
            case 6: return ExperimentRunner.E6_SIZES;
            default: return new int[0];
        }
    }

    private void runOne(int number) {
        String name = "E" + number;
        Console.say("Running " + name + "...");

        long started = System.currentTimeMillis();

        if (number == 1) {
            runner.runE1();
        } else if (number == 2) {
            runner.runE2();
        } else if (number == 3) {
            runner.runE3();
        } else if (number == 4) {
            runner.runE4();
        } else if (number == 5) {
            runner.runE5();
        } else if (number == 6) {
            runner.runE6();
        }
        reportAverages(name, algorithmNamesFor(number), sizesFor(number));

        long took = System.currentTimeMillis() - started;
        Console.say(name + " finished in " + (took / 1000) + " seconds.");
    }

    /** Prints the averaged times, with the warm-up left out. */
    private void reportAverages(String experimentId, String[] algorithms, int[] sizes) {
        Console.blank();
        Console.say(pad("algorithm", 24) + pad("n", 8) + pad("average ms", 14)
                + "warm-up was this much slower");
        Console.say("-----------------------------------------------------------------");

        for (int a = 0; a < algorithms.length; a++) {
            for (int s = 0; s < sizes.length; s++) {
                double average = harness.averageNanos(experimentId, algorithms[a], sizes[s]);
                if (average <= 0) {
                    continue;
                }
                double penalty = harness.warmUpPenalty(experimentId, algorithms[a], sizes[s]);

                Console.say(pad(algorithms[a], 24)
                        + pad(String.valueOf(sizes[s]), 8)
                        + pad(String.format("%.4f", average / 1000000.0), 14)
                        + String.format("%.1f times", penalty));
            }
        }
        Console.blank();
        Console.say("Three runs averaged. The first run is excluded, which is why");
        Console.say("the last column matters: it shows how much difference that made.");
    }

    private void saveToFiles() {
        if (harness.runCount() == 0) {
            Console.problem("Nothing has been measured yet. Run an experiment first.");
            return;
        }
        // The exporter reports a failure by returning a message rather than
        // throwing, so there is nothing to catch here.
        for (int i = 1; i <= 6; i++) {
            String name = "E" + i;
            Console.say(CsvExporter.writeRaw(name, harness.getRuns()));
            Console.say(CsvExporter.writeAverages(name, harness,
                    algorithmNamesFor(i), sizesFor(i)));
        }
        Console.say(CsvExporter.writeReadme());

        Console.blank();
        Console.say("Open these in a spreadsheet to draw the graphs for");
        Console.say("section 10 of the report. The shapes to expect are listed");
        Console.say("in the project guide, Part 26.");
    }

    private void saveToDatabase() {
        if (harness.runCount() == 0) {
            Console.problem("Nothing has been measured yet. Run an experiment first.");
            return;
        }
        try {
            int saved = runDao.saveAll(harness.getRuns());
            Console.say("Saved " + saved + " timing rows.");
            Console.say("The brief asks for at least 30, and we have " + saved + ".");

            if (!runDao.allRunsOnOneMachine()) {
                Console.problem("The table now holds runs from more than one computer. "
                        + "That breaks constraint C21. Clear the table and rerun "
                        + "everything on one machine.");
            }
        } catch (SQLException problem) {
            Console.problem("The database said: " + problem.getMessage());
        }
    }

    private String pad(String text, int width) {
        StringBuilder padded = new StringBuilder(text);
        while (padded.length() < width) {
            padded.append(' ');
        }
        return padded.toString();
    }
}
