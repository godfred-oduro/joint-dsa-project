package gh.ug.hospital.model;

import java.time.LocalDateTime;

/**
 * Owner: 22305896
 *
 * One timing measurement. Matches one row of the algorithm_runs table.
 *
 * The six experiments write these rows, and the graphs in report section 10
 * are drawn from them.
 *
 * Two things the marker will check. Every row must name the same computer,
 * because mixing machines makes the numbers meaningless. And repetition 1 is
 * always thrown away, because Java speeds up as it runs and the first pass
 * can be five to ten times slower than the rest.
 */
public class AlgorithmRun {

    /** The one computer every experiment runs on. */
    public static final String MACHINE =
            "Dell Latitude 7480, Intel Core i5-6300U 2.40GHz 2 cores 4 threads, "
                    + "16GB RAM, Windows 10 Pro 22H2 build 19045, "
                    + "Eclipse Temurin OpenJDK 21.0.5+11 LTS";

    private final int runId;
    private final String experimentId;      // E1 to E6
    private final String algorithmName;
    private final String structureName;     // may be null
    private final int inputSize;
    private final int repetition;           // 1 is the warm-up and is excluded
    private final long elapsedNanos;
    private final Long primitiveOps;        // may be null
    private final String machineSpec;
    private final LocalDateTime runAt;

    public AlgorithmRun(int runId, String experimentId, String algorithmName,
                        String structureName, int inputSize, int repetition,
                        long elapsedNanos, Long primitiveOps,
                        String machineSpec, LocalDateTime runAt) {
        this.runId = runId;
        this.experimentId = experimentId;
        this.algorithmName = algorithmName;
        this.structureName = structureName;
        this.inputSize = inputSize;
        this.repetition = repetition;
        this.elapsedNanos = elapsedNanos;
        this.primitiveOps = primitiveOps;
        this.machineSpec = machineSpec;
        this.runAt = runAt;
    }

    public int getRunId() {
        return runId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public String getStructureName() {
        return structureName;
    }

    public int getInputSize() {
        return inputSize;
    }

    public int getRepetition() {
        return repetition;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public Long getPrimitiveOps() {
        return primitiveOps;
    }

    public String getMachineSpec() {
        return machineSpec;
    }

    public LocalDateTime getRunAt() {
        return runAt;
    }

    public double getElapsedMillis() {
        return elapsedNanos / 1000000.0;
    }

    /** True if this row is the warm-up and must be left out of the average. */
    public boolean isWarmUp() {
        return repetition == 1;
    }

    /** One line for the CSV file that feeds the graphs. */
    public String toCsvLine() {
        StringBuilder line = new StringBuilder();
        line.append(experimentId).append(",");
        line.append(algorithmName).append(",");
        if (structureName == null) {
            line.append(",");
        } else {
            line.append(structureName).append(",");
        }
        line.append(inputSize).append(",");
        line.append(repetition).append(",");
        line.append(elapsedNanos).append(",");
        if (primitiveOps == null) {
            line.append(",");
        } else {
            line.append(primitiveOps).append(",");
        }
        line.append(isWarmUp());
        return line.toString();
    }

    /** The header row for that CSV file. */
    public static String csvHeader() {
        return "experiment_id,algorithm_name,structure_name,input_size,"
                + "repetition,elapsed_ns,primitive_ops,is_warm_up";
    }

    @Override
    public String toString() {
        return experimentId + " " + algorithmName + " n=" + inputSize
                + " rep=" + repetition + " " + String.format("%.3f", getElapsedMillis()) + " ms";
    }
}
