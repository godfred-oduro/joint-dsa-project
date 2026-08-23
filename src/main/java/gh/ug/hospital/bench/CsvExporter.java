package gh.ug.hospital.bench;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.AlgorithmRun;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Owner: 22224236
 *
 * Writes the timing results to CSV files so they can be plotted.
 *
 * Two files come out of each experiment.
 *
 * The raw file holds every single run, warm-up included, so a marker can see
 * that the warm-up was taken and then set aside rather than never measured.
 *
 * The averages file holds one row per algorithm per size, with the warm-up
 * left out. That is the file that goes into a spreadsheet to draw the graph.
 */
public class CsvExporter {

    /** Where the files are written. */
    public static final String OUTPUT_FOLDER = "output";

    /**
     * Writes every run to a file.
     *
     * @return the path written, or a message explaining why it failed
     */
    public static String writeRaw(String experimentId, DynamicArray<AlgorithmRun> runs) {
        String path = OUTPUT_FOLDER + File.separator + experimentId + "_raw.csv";

        PrintWriter writer = null;
        try {
            makeFolder();
            writer = new PrintWriter(new FileWriter(path));
            writer.println(AlgorithmRun.csvHeader());

            // Only write the rows that belong to this experiment. Without this
            // filter every "E_raw.csv" file ends up holding every run ever
            // taken, because harness.getRuns() returns the whole history.
            int written = 0;
            for (int i = 0; i < runs.size(); i++) {
                AlgorithmRun run = runs.get(i);
                if (run.getExperimentId().equals(experimentId)) {
                    writer.println(run.toCsvLine());
                    written++;
                }
            }
            return "Wrote " + written + " rows to " + path;

        } catch (IOException problem) {
            return "Could not write " + path + ": " + problem.getMessage();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Writes the averaged results, which is what the graphs are drawn from.
     */
    public static String writeAverages(String experimentId, TimingHarness harness,
                                       String[] algorithmNames, int[] sizes) {
        String path = OUTPUT_FOLDER + File.separator + experimentId + "_averages.csv";

        PrintWriter writer = null;
        try {
            makeFolder();
            writer = new PrintWriter(new FileWriter(path));
            writer.println("algorithm,input_size,average_ns,average_ms,runs_averaged");

            int rows = 0;
            for (int a = 0; a < algorithmNames.length; a++) {
                for (int s = 0; s < sizes.length; s++) {
                    // Filtered by experimentId, not the two-argument overload,
                    // so two experiments measuring the same algorithm at the
                    // same size (e.g. BST appearing in both E4 and elsewhere)
                    // do not get averaged into each other.
                    double averageNanos = harness.averageNanos(experimentId, algorithmNames[a], sizes[s]);
                    if (averageNanos == 0.0) {
                        continue;   // that combination was never measured
                    }
                    writer.println(algorithmNames[a] + ","
                            + sizes[s] + ","
                            + String.format("%.0f", averageNanos) + ","
                            + String.format("%.4f", averageNanos / 1000000.0) + ","
                            + (TimingHarness.REPETITIONS - 1));
                    rows++;
                }
            }
            return "Wrote " + rows + " averaged rows to " + path
                    + "  (the warm-up run was excluded from every average)";

        } catch (IOException problem) {
            return "Could not write " + path + ": " + problem.getMessage();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Writes a note beside the results saying which computer they came from
     * and how they were taken. Without this the numbers cannot be checked.
     *
     * This never throws. If the file cannot be written it hands back a
     * message saying so, which the menu prints.
     */
    public static String writeReadme() {
        String path = OUTPUT_FOLDER + File.separator + "machine_and_method.txt";

        PrintWriter writer = null;
        try {
            makeFolder();
            writer = new PrintWriter(new FileWriter(path));
            writer.println("How the timing results were taken");
            writer.println("=================================");
            writer.println();
            writer.println("Computer:");
            writer.println("  " + AlgorithmRun.MACHINE);
            writer.println();
            writer.println("Method:");
            writer.println("  Each measurement was run "
                    + TimingHarness.REPETITIONS + " times.");
            writer.println("  Run 1 is the warm-up and is excluded from every average,");
            writer.println("  because Java compiles code to a faster form after it has");
            writer.println("  run a few thousand times. The remaining "
                    + (TimingHarness.REPETITIONS - 1) + " runs are averaged.");
            writer.println();
            writer.println("  Times come from System.nanoTime, not currentTimeMillis,");
            writer.println("  because the millisecond clock on Windows only updates");
            writer.println("  about every 15.6 ms and would show small tests as zero.");
            writer.println();
            writer.println("  The program was started with -Xms2g -Xmx2g so that Java");
            writer.println("  never resizes its memory in the middle of a run.");
            writer.println();
            writer.println("Every row in every file names the same computer. If that is");
            writer.println("not true, the results have been mixed and cannot be used.");

            return "Wrote " + path;

        } catch (IOException problem) {
            return "Could not write " + path + ": " + problem.getMessage();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void makeFolder() {
        File folder = new File(OUTPUT_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }
}
