package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.AlgorithmRun;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Owner: 22305896
 *
 * Reads and writes the algorithm_runs table, where the timing results go.
 *
 * The six experiments write these rows and the graphs in report section 10
 * are drawn from them. Two rules matter here.
 *
 * Every row records the same computer. Mixing machines would make the numbers
 * meaningless, so the machine string comes from one constant.
 *
 * Repetition 1 is always the warm-up. Java speeds up as it runs, so the first
 * pass can be five to ten times slower than the rest. The averaging query
 * below leaves it out, and the report says so.
 */
public class AlgorithmRunDao {

    private static final String INSERT =
            "INSERT INTO algorithm_runs (experiment_id, algorithm_name, structure_name, "
                    + "input_size, repetition, elapsed_ns, primitive_ops, machine_spec, run_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_EXPERIMENT =
            "SELECT run_id, experiment_id, algorithm_name, structure_name, input_size, "
                    + "repetition, elapsed_ns, primitive_ops, machine_spec, run_at "
                    + "FROM algorithm_runs WHERE experiment_id = ? "
                    + "ORDER BY algorithm_name, input_size, repetition";

    /** Averages the good runs, leaving out the warm-up pass. */
    private static final String AVERAGES =
            "SELECT algorithm_name, input_size, "
                    + "AVG(elapsed_ns) AS average_ns, COUNT(*) AS runs_used "
                    + "FROM algorithm_runs "
                    + "WHERE experiment_id = ? AND repetition > 1 "
                    + "GROUP BY algorithm_name, input_size "
                    + "ORDER BY algorithm_name, input_size";

    private static final String COUNT = "SELECT COUNT(*) FROM algorithm_runs";

    private static final String DISTINCT_MACHINES =
            "SELECT COUNT(DISTINCT machine_spec) FROM algorithm_runs";

    public int saveAll(DynamicArray<AlgorithmRun> runs) throws SQLException {
        int saved = 0;

        try (Connection connection = ConnectionManager.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                for (int i = 0; i < runs.size(); i++) {
                    AlgorithmRun run = runs.get(i);

                    statement.setString(1, run.getExperimentId());
                    statement.setString(2, run.getAlgorithmName());
                    statement.setString(3, run.getStructureName());
                    statement.setInt(4, run.getInputSize());
                    statement.setInt(5, run.getRepetition());
                    statement.setLong(6, run.getElapsedNanos());

                    if (run.getPrimitiveOps() == null) {
                        statement.setNull(7, java.sql.Types.BIGINT);
                    } else {
                        statement.setLong(7, run.getPrimitiveOps().longValue());
                    }

                    statement.setString(8, run.getMachineSpec());
                    statement.setTimestamp(9, Timestamp.valueOf(run.getRunAt()));
                    statement.addBatch();
                }
                saved = statement.executeBatch().length;
                connection.commit();
            } catch (SQLException problem) {
                connection.rollback();
                throw problem;
            }
        }
        return saved;
    }

    public DynamicArray<AlgorithmRun> findByExperiment(String experimentId)
            throws SQLException {
        DynamicArray<AlgorithmRun> found = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement =
                     connection.prepareStatement(SELECT_BY_EXPERIMENT)) {

            statement.setString(1, experimentId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(new AlgorithmRun(
                            rows.getInt("run_id"),
                            rows.getString("experiment_id"),
                            rows.getString("algorithm_name"),
                            rows.getString("structure_name"),
                            rows.getInt("input_size"),
                            rows.getInt("repetition"),
                            rows.getLong("elapsed_ns"),
                            Long.valueOf(rows.getLong("primitive_ops")),
                            rows.getString("machine_spec"),
                            rows.getTimestamp("run_at").toLocalDateTime()));
                }
            }
        }
        return found;
    }

    /**
     * The averaged results for one experiment, ready to plot.
     * The warm-up pass is left out.
     */
    public String averagesFor(String experimentId) throws SQLException {
        StringBuilder out = new StringBuilder();
        out.append("algorithm,input_size,average_ns,runs_used\n");

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(AVERAGES)) {

            statement.setString(1, experimentId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    out.append(rows.getString("algorithm_name")).append(",")
                            .append(rows.getInt("input_size")).append(",")
                            .append(String.format("%.0f", rows.getDouble("average_ns")))
                            .append(",")
                            .append(rows.getInt("runs_used")).append("\n");
                }
            }
        }
        return out.toString();
    }

    public int count() throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(COUNT);
             ResultSet rows = statement.executeQuery()) {

            if (rows.next()) {
                return rows.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Checks every timing row names the same computer.
     *
     * If this comes back with more than 1, someone ran an experiment on a
     * different machine and the graphs cannot be trusted. That breaks
     * constraint C21.
     */
    public boolean allRunsOnOneMachine() throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(DISTINCT_MACHINES);
             ResultSet rows = statement.executeQuery()) {

            if (rows.next()) {
                return rows.getInt(1) <= 1;
            }
        }
        return true;
    }
}
