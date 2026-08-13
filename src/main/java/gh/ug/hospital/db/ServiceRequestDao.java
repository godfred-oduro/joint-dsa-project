package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.ServiceRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Owner: 22367946
 *
 * Reads and writes the service_requests table.
 *
 * All 300 rows are made up. They were generated from the hospital's
 * published clinic timetable, the published list of services, the real
 * triage scale and our surveyed location codes. Every row carries
 * source = SYNTHETIC so nobody can mistake them for real patient records.
 *
 * This is also the only table the program writes to while it runs, because
 * assigning a job changes a request's status. The brief asks for a program
 * that both reads and writes the database, and this is where that happens.
 */
public class ServiceRequestDao {

    private static final String COLUMNS =
            "request_id, source_location_id, destination_location_id, category, "
                    + "urgency, time_submitted, deadline, status, sats_colour, "
                    + "tews_score, patient_class, requires_gp_referral, "
                    + "eligible_at_clinic, assigned_resource_id, target_clinic, "
                    + "porter_minutes, source";

    private static final String SELECT_ALL =
            "SELECT " + COLUMNS + " FROM service_requests ORDER BY request_id";

    private static final String SELECT_BY_STATUS =
            "SELECT " + COLUMNS + " FROM service_requests WHERE status = ? "
                    + "ORDER BY urgency DESC, deadline ASC";

    private static final String SELECT_ONE =
            "SELECT " + COLUMNS + " FROM service_requests WHERE request_id = ?";

    private static final String INSERT =
            "INSERT INTO service_requests (" + COLUMNS + ") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "status = VALUES(status), deadline = VALUES(deadline), "
                    + "assigned_resource_id = VALUES(assigned_resource_id)";

    private static final String UPDATE_ASSIGNMENT =
            "UPDATE service_requests SET status = ?, assigned_resource_id = ?, "
                    + "deadline = ? WHERE request_id = ?";

    private static final String COUNT = "SELECT COUNT(*) FROM service_requests";

    private static final String COUNT_BY_COLOUR =
            "SELECT sats_colour, COUNT(*) AS how_many FROM service_requests "
                    + "GROUP BY sats_colour ORDER BY MAX(urgency) DESC";

    public DynamicArray<ServiceRequest> findAll() throws SQLException {
        DynamicArray<ServiceRequest> all = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                all.add(readRow(rows));
            }
        }
        return all;
    }

    /** Everything at one stage, for example everything still waiting. */
    public DynamicArray<ServiceRequest> findByStatus(String status) throws SQLException {
        DynamicArray<ServiceRequest> found = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_STATUS)) {

            statement.setString(1, status);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(readRow(rows));
                }
            }
        }
        return found;
    }

    public ServiceRequest findById(String requestId) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_ONE)) {

            statement.setString(1, requestId);

            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return readRow(rows);
                }
            }
        }
        return null;
    }

    /**
     * Writes an assignment back to the database.
     *
     * This is the write half of the database requirement. When the dispatcher
     * picks a porter, that decision has to survive the program closing.
     */
    public void saveAssignment(ServiceRequest request) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ASSIGNMENT)) {

            statement.setString(1, request.getStatus());
            statement.setString(2, request.getAssignedResourceId());
            statement.setTimestamp(3, Timestamp.valueOf(request.getDeadline()));
            statement.setString(4, request.getRequestId());
            statement.executeUpdate();
        }
    }

    public int saveAll(DynamicArray<ServiceRequest> requests) throws SQLException {
        int saved = 0;

        try (Connection connection = ConnectionManager.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                for (int i = 0; i < requests.size(); i++) {
                    fillStatement(statement, requests.get(i));
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

    /**
     * How many requests of each colour there are.
     *
     * Menu option M1 prints this so we can check the generated data actually
     * matches the mix the guide asks for: 12 red, 33 orange, 120 yellow,
     * 132 green, 3 blue.
     */
    public String countByColour() throws SQLException {
        StringBuilder out = new StringBuilder();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(COUNT_BY_COLOUR);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                out.append(rows.getString("sats_colour"))
                        .append(": ")
                        .append(rows.getInt("how_many"))
                        .append("\n");
            }
        }
        return out.toString();
    }

    private ServiceRequest readRow(ResultSet rows) throws SQLException {
        ServiceRequest request = new ServiceRequest(
                rows.getString("request_id"),
                rows.getString("source_location_id"),
                rows.getString("destination_location_id"),
                rows.getString("category"),
                rows.getInt("urgency"),
                readTimeOrNull(rows, "time_submitted"),
                readTimeOrNull(rows, "deadline"),
                rows.getString("status"),
                rows.getString("sats_colour"),
                readIntOrNull(rows, "tews_score"),
                rows.getString("patient_class"),
                rows.getBoolean("requires_gp_referral"),
                rows.getBoolean("eligible_at_clinic"),
                rows.getString("target_clinic"),
                rows.getInt("porter_minutes"));

        request.setAssignedResourceId(rows.getString("assigned_resource_id"));
        return request;
    }

    private LocalDateTime readTimeOrNull(ResultSet rows, String column) throws SQLException {
        Timestamp value = rows.getTimestamp(column);
        if (value == null) {
            return null;
        }
        return value.toLocalDateTime();
    }

    private Integer readIntOrNull(ResultSet rows, String column) throws SQLException {
        int value = rows.getInt(column);
        if (rows.wasNull()) {
            return null;
        }
        return Integer.valueOf(value);
    }

    private void fillStatement(PreparedStatement statement, ServiceRequest request)
            throws SQLException {
        statement.setString(1, request.getRequestId());
        statement.setString(2, request.getSourceLocationId());
        statement.setString(3, request.getDestinationLocationId());
        statement.setString(4, request.getCategory());
        statement.setInt(5, request.getUrgency());
        statement.setTimestamp(6, Timestamp.valueOf(request.getTimeSubmitted()));
        statement.setTimestamp(7, Timestamp.valueOf(request.getDeadline()));
        statement.setString(8, request.getStatus());
        statement.setString(9, request.getSatsColour());

        if (request.getTewsScore() == null) {
            statement.setNull(10, java.sql.Types.TINYINT);
        } else {
            statement.setInt(10, request.getTewsScore().intValue());
        }

        statement.setString(11, request.getPatientClass());
        statement.setBoolean(12, request.requiresGpReferral());
        statement.setBoolean(13, request.isEligibleAtClinic());
        statement.setString(14, request.getAssignedResourceId());
        statement.setString(15, request.getTargetClinic());
        statement.setInt(16, request.getPorterMinutes());
        statement.setString(17, "SYNTHETIC");
    }
}
