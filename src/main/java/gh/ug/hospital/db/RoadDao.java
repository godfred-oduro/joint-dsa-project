package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.Road;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Owner: 22392509
 *
 * Reads and writes the roads table.
 *
 * The two observed-time columns can be empty, because nobody has walked the
 * paths with a stopwatch yet. Java's ResultSet returns 0 for an empty number
 * column, which would look like a real measurement of zero minutes, so we
 * check wasNull() every time and store null instead.
 */
public class RoadDao {

    private static final String COLUMNS =
            "road_id, from_location_id, to_location_id, distance_km, travel_time_min, "
                    + "condition_weight, road_type, surface, covered, distance_m, "
                    + "walk_time_min, weighted_cost_min, is_accessible, congestion_zone, "
                    + "obs_time_0900_min, obs_time_1500_min, source, confidence, notes";

    private static final String SELECT_ALL =
            "SELECT " + COLUMNS + " FROM roads ORDER BY road_id";

    private static final String SELECT_TOUCHING =
            "SELECT " + COLUMNS + " FROM roads "
                    + "WHERE from_location_id = ? OR to_location_id = ?";

    private static final String INSERT =
            "INSERT INTO roads (" + COLUMNS + ") "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "distance_km = VALUES(distance_km), "
                    + "travel_time_min = VALUES(travel_time_min), "
                    + "condition_weight = VALUES(condition_weight), "
                    + "weighted_cost_min = VALUES(weighted_cost_min), "
                    + "obs_time_0900_min = VALUES(obs_time_0900_min), "
                    + "obs_time_1500_min = VALUES(obs_time_1500_min), "
                    + "notes = VALUES(notes)";

    private static final String UPDATE_MEASURED =
            "UPDATE roads SET obs_time_0900_min = ?, obs_time_1500_min = ?, "
                    + "source = 'FIELD_SURVEY', confidence = 'A' WHERE road_id = ?";

    private static final String COUNT = "SELECT COUNT(*) FROM roads";

    public DynamicArray<Road> findAll() throws SQLException {
        DynamicArray<Road> all = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                all.add(readRow(rows));
            }
        }
        return all;
    }

    /** Every path with one end at the given place. */
    public DynamicArray<Road> findTouching(String locationId) throws SQLException {
        DynamicArray<Road> found = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_TOUCHING)) {

            statement.setString(1, locationId);
            statement.setString(2, locationId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(readRow(rows));
                }
            }
        }
        return found;
    }

    public int saveAll(DynamicArray<Road> roads) throws SQLException {
        int saved = 0;

        try (Connection connection = ConnectionManager.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                for (int i = 0; i < roads.size(); i++) {
                    fillStatement(statement, roads.get(i));
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

    /**
     * Records the two stopwatch readings taken during the visit, and marks
     * the row as measured rather than estimated.
     *
     * This is how the estimated values in the guide get replaced: by changing
     * data, not by changing code.
     */
    public void recordMeasuredTimes(String roadId, Double morning, Double afternoon)
            throws SQLException {

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE_MEASURED)) {

            setDoubleOrNull(statement, 1, morning);
            setDoubleOrNull(statement, 2, afternoon);
            statement.setString(3, roadId);
            statement.executeUpdate();
        }
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

    private Road readRow(ResultSet rows) throws SQLException {
        Road road = new Road(
                rows.getString("road_id"),
                rows.getString("from_location_id"),
                rows.getString("to_location_id"),
                rows.getDouble("distance_km"),
                rows.getDouble("travel_time_min"),
                rows.getDouble("condition_weight"),
                rows.getString("road_type"),
                rows.getString("surface"),
                rows.getString("covered"),
                rows.getDouble("distance_m"),
                rows.getDouble("walk_time_min"),
                rows.getDouble("weighted_cost_min"),
                rows.getString("is_accessible"),
                rows.getString("congestion_zone"),
                rows.getString("source"),
                rows.getString("confidence"),
                rows.getString("notes"));

        road.setObservedTime0900(readDoubleOrNull(rows, "obs_time_0900_min"));
        road.setObservedTime1500(readDoubleOrNull(rows, "obs_time_1500_min"));
        return road;
    }

    /**
     * Reads a number that might be empty.
     *
     * getDouble returns 0 for an empty column, which would look like a real
     * reading of zero minutes. wasNull tells us which it really was.
     */
    private Double readDoubleOrNull(ResultSet rows, String column) throws SQLException {
        double value = rows.getDouble(column);
        if (rows.wasNull()) {
            return null;
        }
        return Double.valueOf(value);
    }

    private void setDoubleOrNull(PreparedStatement statement, int position, Double value)
            throws SQLException {
        if (value == null) {
            statement.setNull(position, java.sql.Types.DECIMAL);
        } else {
            statement.setDouble(position, value.doubleValue());
        }
    }

    private void fillStatement(PreparedStatement statement, Road road) throws SQLException {
        statement.setString(1, road.getRoadId());
        statement.setString(2, road.getFromLocationId());
        statement.setString(3, road.getToLocationId());
        statement.setDouble(4, road.getDistanceKm());
        statement.setDouble(5, road.getTravelTimeMinutes());
        statement.setDouble(6, road.getConditionWeight());
        statement.setString(7, road.getRoadType());
        statement.setString(8, road.getSurface());
        statement.setString(9, road.getCovered());
        statement.setDouble(10, road.getDistanceMetres());
        statement.setDouble(11, road.getWalkTimeMinutes());
        statement.setDouble(12, road.getWeightedCostMinutes());
        statement.setString(13, road.getIsAccessible());
        statement.setString(14, road.getCongestionZone());
        setDoubleOrNull(statement, 15, road.getObservedTime0900());
        setDoubleOrNull(statement, 16, road.getObservedTime1500());
        statement.setString(17, road.getSource());
        statement.setString(18, road.getConfidence());
        statement.setString(19, road.getNotes());
    }
}
