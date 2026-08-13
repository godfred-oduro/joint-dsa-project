package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.Resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;

/**
 * Owner: 22395395
 *
 * Reads and writes the resources table: porters, nurses, wheelchairs,
 * trolleys, ambulances.
 *
 * This table is the one an examiner is most likely to question, because
 * every count in it was worked out rather than counted. Each row carries
 * confidence D to say so plainly. When someone walks the hospital and counts
 * the real wheelchairs, the row is updated and the confidence becomes A. No
 * code changes.
 */
public class ResourceDao {

    private static final String COLUMNS =
            "resource_id, resource_type, home_location_id, capacity, "
                    + "availability_status, shift, available_from, available_to, "
                    + "source, confidence";

    private static final String SELECT_ALL =
            "SELECT " + COLUMNS + " FROM resources ORDER BY resource_id";

    private static final String SELECT_BY_TYPE =
            "SELECT " + COLUMNS + " FROM resources WHERE resource_type = ? "
                    + "ORDER BY resource_id";

    private static final String INSERT =
            "INSERT INTO resources (" + COLUMNS + ") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "capacity = VALUES(capacity), "
                    + "availability_status = VALUES(availability_status), "
                    + "shift = VALUES(shift), source = VALUES(source), "
                    + "confidence = VALUES(confidence)";

    private static final String UPDATE_STATUS =
            "UPDATE resources SET availability_status = ? WHERE resource_id = ?";

    private static final String COUNT = "SELECT COUNT(*) FROM resources";

    public DynamicArray<Resource> findAll() throws SQLException {
        DynamicArray<Resource> all = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                all.add(readRow(rows));
            }
        }
        return all;
    }

    /** Everything of one kind, for example every porter. */
    public DynamicArray<Resource> findByType(String resourceType) throws SQLException {
        DynamicArray<Resource> found = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_TYPE)) {

            statement.setString(1, resourceType);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(readRow(rows));
                }
            }
        }
        return found;
    }

    /** Writes a resource's new state back, for example after it takes a job. */
    public void updateStatus(String resourceId, String status) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE_STATUS)) {

            statement.setString(1, status);
            statement.setString(2, resourceId);
            statement.executeUpdate();
        }
    }

    public int saveAll(DynamicArray<Resource> resources) throws SQLException {
        int saved = 0;

        try (Connection connection = ConnectionManager.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                for (int i = 0; i < resources.size(); i++) {
                    fillStatement(statement, resources.get(i));
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

    private Resource readRow(ResultSet rows) throws SQLException {
        return new Resource(
                rows.getString("resource_id"),
                rows.getString("resource_type"),
                rows.getString("home_location_id"),
                rows.getInt("capacity"),
                rows.getString("availability_status"),
                rows.getString("shift"),
                readTimeOrNull(rows, "available_from"),
                readTimeOrNull(rows, "available_to"),
                rows.getString("source"),
                rows.getString("confidence"));
    }

    private LocalTime readTimeOrNull(ResultSet rows, String column) throws SQLException {
        Time value = rows.getTime(column);
        if (value == null) {
            return null;
        }
        return value.toLocalTime();
    }

    private void fillStatement(PreparedStatement statement, Resource resource)
            throws SQLException {
        statement.setString(1, resource.getResourceId());
        statement.setString(2, resource.getResourceType());
        statement.setString(3, resource.getHomeLocationId());
        statement.setInt(4, resource.getCapacity());
        statement.setString(5, resource.getAvailabilityStatus());
        statement.setString(6, resource.getShift());
        setTimeOrNull(statement, 7, resource.getAvailableFrom());
        setTimeOrNull(statement, 8, resource.getAvailableTo());
        statement.setString(9, resource.getSource());
        statement.setString(10, resource.getConfidence());
    }

    private void setTimeOrNull(PreparedStatement statement, int position, LocalTime time)
            throws SQLException {
        if (time == null) {
            statement.setNull(position, java.sql.Types.TIME);
        } else {
            statement.setTime(position, Time.valueOf(time));
        }
    }
}
