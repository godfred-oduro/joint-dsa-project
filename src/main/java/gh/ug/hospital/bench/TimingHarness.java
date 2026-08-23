package gh.ug.hospital.bench;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.AlgorithmRun;
import gh.ug.hospital.util.AlgorithmResult;

import java.time.LocalDateTime;

/**
 * Owner: 22224236
 *
 * Times a piece of work properly.
 *
 * Timing Java is not as simple as looking at the clock twice. Three things
 * would spoil the numbers if we ignored them.
 *
 * Java speeds up as it runs. The first time a method is called it is
 * interpreted slowly. After a few thousand calls the compiler rewrites it
 * into fast machine code. So the first run of any test can be five to ten
 * times slower than the rest. We therefore run each measurement four times
 * and throw the first one away. The project brief asks for at least three
 * runs averaged, so four runs minus the warm-up gives exactly that.
 *
 * The clock matters. System.currentTimeMillis only updates about every
 * 15.6 milliseconds on Windows, so a test taking two milliseconds would
 * measure as zero. System.nanoTime is far finer and is what we use.
 *
 * Java tidies up memory while the program runs, and that pause lands in the
 * middle of measurements. We cannot stop it, but starting the program with
 * -Xms2g -Xmx2g stops it resizing its memory, which is the worst of it.
 */
public class TimingHarness {

    /** How many times each measurement is repeated, including the warm-up. */
    public static final int REPETITIONS = 4;

    /** Which repetition is the warm-up and gets thrown away. */
    public static final int WARM_UP_REPETITION = 1;

    /**
     * A piece of work that can be timed.
     *
     * We use a small interface rather than a lambda so that anyone reading
     * this can see exactly what is happening. Every algorithm in the project
     * hands back an AlgorithmResult, which already carries the counts of
     * comparisons and swaps, so one run gives us both the time and the
     * operation count.
     */
    public interface Timeable {
        /** Does the work once and hands back what the algorithm produced. */
        AlgorithmResult runOnce();
    }

    private final DynamicArray<AlgorithmRun> results;
    private int nextRunId;

    public TimingHarness() {
        this.results = new DynamicArray<>();
        this.nextRunId = 1;
    }

    /**
     * Runs one measurement four times and records all four.
     *
     * The warm-up row is kept rather than discarded, because the report has
     * to be able to show that it was excluded rather than never taken.
     */
    public void measure(String experimentId, String algorithmName,
                        String structureName, int inputSize, Timeable work) {

        for (int repetition = 1; repetition <= REPETITIONS; repetition++) {

            // Ask Java to tidy memory before we start, so a collection is
            // less likely to land in the middle of the measurement.
            System.gc();

            long startedAt = System.nanoTime();
            AlgorithmResult produced = work.runOnce();
            long elapsed = System.nanoTime() - startedAt;

            // The algorithm counted its own comparisons and swaps as it ran,
            // so we get the operation count for free from the same run.
            Long operationBox = null;
            if (produced != null) {
                operationBox = Long.valueOf(produced.getPrimitiveOperations());
            }

            results.add(new AlgorithmRun(
                    nextRunId,
                    experimentId,
                    algorithmName,
                    structureName,
                    inputSize,
                    repetition,
                    elapsed,
                    operationBox,
                    AlgorithmRun.MACHINE,
                    LocalDateTime.now()));

            nextRunId++;
        }
    }

    /**
     * The average time for one algorithm at one size, leaving out the
     * warm-up run. This is the number that gets plotted.
     */
    public double averageNanos(String algorithmName, int inputSize) {
        return averageNanos(null, algorithmName, inputSize);
    }

    /**
     * The same average, but only counting runs from one experiment.
     *
     * Two experiments can measure the same algorithm at the same size, so
     * without the experiment code the averages would run into each other.
     * Pass null for the experiment to average across all of them.
     */
    public double averageNanos(String experimentId, String algorithmName, int inputSize) {
        long total = 0;
        int used = 0;

        for (int i = 0; i < results.size(); i++) {
            AlgorithmRun run = results.get(i);
            if (run.isWarmUp()) {
                continue;
            }
            if (experimentId != null && !run.getExperimentId().equals(experimentId)) {
                continue;
            }
            if (run.getAlgorithmName().equals(algorithmName)
                    && run.getInputSize() == inputSize) {
                total += run.getElapsedNanos();
                used++;
            }
        }

        if (used == 0) {
            return 0.0;
        }
        return (double) total / (double) used;
    }

    /**
     * How many times slower the warm-up run was than the average of the rest.
     *
     * This is the figure that justifies throwing the first run away. Java
     * interprets code slowly at first, then compiles it into fast machine
     * code once it has run a few thousand times. A result of 5.0 means the
     * first run took five times as long as the ones we actually counted.
     *
     * Returns 1.0 when there is nothing to compare, which means no penalty.
     */
    public double warmUpPenalty(String experimentId, String algorithmName, int inputSize) {
        long warmUpTime = 0;

        for (int i = 0; i < results.size(); i++) {
            AlgorithmRun run = results.get(i);
            if (!run.isWarmUp()) {
                continue;
            }
            if (experimentId != null && !run.getExperimentId().equals(experimentId)) {
                continue;
            }
            if (run.getAlgorithmName().equals(algorithmName)
                    && run.getInputSize() == inputSize) {
                warmUpTime = run.getElapsedNanos();
                break;
            }
        }

        double rest = averageNanos(experimentId, algorithmName, inputSize);
        if (warmUpTime == 0 || rest <= 0) {
            return 1.0;
        }
        return warmUpTime / rest;
    }

    /** Every row taken so far, warm-ups included. */
    public DynamicArray<AlgorithmRun> allRuns() {
        return results;
    }

    /** The same list, under the name the menus use. */
    public DynamicArray<AlgorithmRun> getRuns() {
        return results;
    }

    /** Only the rows that count towards the averages. */
    public DynamicArray<AlgorithmRun> goodRuns() {
        DynamicArray<AlgorithmRun> good = new DynamicArray<>();
        for (int i = 0; i < results.size(); i++) {
            if (!results.get(i).isWarmUp()) {
                good.add(results.get(i));
            }
        }
        return good;
    }

    public int runCount() {
        return results.size();
    }

    public void clear() {
        results.clear();
        nextRunId = 1;
    }

    /**
     * Warns about anything that would make the numbers untrustworthy.
     * Printed before every experiment so nobody forgets.
     */
    public static String healthWarnings() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
        long freeMemoryMb = runtime.freeMemory() / (1024 * 1024);

        StringBuilder out = new StringBuilder();
        out.append("Before timing, check these:\n");
        out.append("  Java has ").append(maxMemoryMb)
                .append(" MB of memory to use, ").append(freeMemoryMb)
                .append(" MB free right now.\n");

        if (maxMemoryMb < 1800) {
            out.append("  WARNING: start the program with -Xms2g -Xmx2g. ")
                    .append("Without it Java resizes its memory mid-run and the ")
                    .append("timings wander.\n");
        }

        out.append("  Processors available: ").append(runtime.availableProcessors())
                .append("\n");
        out.append("  Close other programs, plug the laptop in, and set the ")
                .append("power plan to High Performance.\n");
        out.append("  Run ").append(REPETITIONS)
                .append(" times at each size and ignore the first.\n");
        return out.toString();
    }
}
