package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Owner: 22399601
 *
 * Reads and writes the locations table.
 *
 * DAO stands for Data Access Object. The idea is that every piece of SQL in
 * the project lives in this package and nowhere else, so the algorithms and
 * the data structures never touch the database directly. That is constraint
 * C20, and it also means the whole program could be pointed at a different
 * database by changing only these files.
 *
 * We use PreparedStatement rather than building SQL by joining strings
 * together. That is safer and it is also faster, because the database can
 * work out its plan once and reuse it.
 */
public class LocationDao {

    private static final String SELECT_ALL =
            "SELECT location_id, name, area, location_type, x_coord, y_coord, "
                    + "internal_code, zone, source, confidence, notes "
                    + "FROM locations ORDER BY location_id";

    private static final String SELECT_ONE =
            "SELECT location_id, name, area, location_type, x_coord, y_coord, "
                    + "internal_code, zone, source, confidence, notes "
                    + "FROM locations WHERE location_id = ?";

    private static final String INSERT =
            "INSERT INTO locations (location_id, name, area, location_type, "
                    + "x_coord, y_coord, internal_code, zone, source, confidence, notes) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "name = VALUES(name), area = VALUES(area), "
                    + "location_type = VALUES(location_type), "
                    + "x_coord = VALUES(x_coord), y_coord = VALUES(y_coord), "
                    + "internal_code = VALUES(internal_code), zone = VALUES(zone), "
                    + "source = VALUES(source), confidence = VALUES(confidence), "
                    + "notes = VALUES(notes)";

    private static final String COUNT = "SELECT COUNT(*) FROM locations";

    /** Reads every location. */
    public DynamicArray<Location> findAll() throws SQLException {
        DynamicArray<Location> all = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet rows = statement.executeQuery()) {

            while (rows.next()) {
                all.add(readRow(rows));
            }
        }
        return all;
    }

    /** Reads one location by its code, or null if there is no such place. */
    public Location findById(String locationId) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_ONE)) {

            statement.setString(1, locationId);

            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    return readRow(rows);
                }
            }
        }
        return null;
    }

    /**
     * Saves a location. If the code is already there the row is updated
     * rather than duplicated, so loading the seed data twice is harmless.
     */
    public void save(Location location) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            fillStatement(statement, location);
            statement.executeUpdate();
        }
    }

    /**
     * Saves many locations in one go.
     *
     * We turn auto-commit off and send them as a batch. Doing 52 separate
     * round trips to the database is far slower than doing one.
     */
    public int saveAll(DynamicArray<Location> locations) throws SQLException {
        int saved = 0;

        try (Connection connection = ConnectionManager.open()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                for (int i = 0; i < locations.size(); i++) {
                    fillStatement(statement, locations.get(i));
                    statement.addBatch();
                }
                int[] results = statement.executeBatch();
                saved = results.length;
                connection.commit();
            } catch (SQLException problem) {
                connection.rollback();
                throw problem;
            }
        }
        return saved;
    }

    /** How many rows the table holds. Menu option M1 shows this. */
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

    /** Turns one row of the result into a Location object. */
    private Location readRow(ResultSet rows) throws SQLException {
        return new Location(
                rows.getString("location_id"),
                rows.getString("name"),
                rows.getString("area"),
                rows.getString("location_type"),
                rows.getDouble("x_coord"),
                rows.getDouble("y_coord"),
                rows.getString("internal_code"),
                rows.getString("zone"),
                rows.getString("source"),
                rows.getString("confidence"),
                rows.getString("notes"));
    }

    /** Puts a Location's fields into the question marks of the insert. */
    private void fillStatement(PreparedStatement statement, Location location)
            throws SQLException {
        statement.setString(1, location.getLocationId());
        statement.setString(2, location.getName());
        statement.setString(3, location.getArea());
        statement.setString(4, location.getLocationType());
        statement.setDouble(5, location.getLatitude());
        statement.setDouble(6, location.getLongitude());
        statement.setString(7, location.getInternalCode());
        statement.setString(8, location.getZone());
        statement.setString(9, location.getSource());
        statement.setString(10, location.getConfidence());
        statement.setString(11, location.getNotes());
    }
}
