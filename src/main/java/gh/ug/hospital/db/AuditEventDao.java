package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.AuditEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Owner: 22370298
 *
 * Reads and writes the audit_events table.
 *
 * Every decision the program makes is recorded here, along with what the
 * data looked like beforehand. That second part is what makes undo work: the
 * stack pops the most recent event and puts the old state back (rule BR14).
 *
 * It also means the whole session can be replayed afterwards, which is
 * useful evidence for the report and for the demonstration video.
 */
public class AuditEventDao {

    private static final String INSERT =
            "INSERT INTO audit_events (event_type, entity_table, entity_id, actor, "
                    + "payload, undo_payload, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_RECENT =
            "SELECT event_id, event_type, entity_table, entity_id, actor, "
                    + "payload, undo_payload, created_at "
                    + "FROM audit_events ORDER BY event_id DESC LIMIT ?";

    private static final String SELECT_FOR_ENTITY =
            "SELECT event_id, event_type, entity_table, entity_id, actor, "
                    + "payload, undo_payload, created_at "
                    + "FROM audit_events WHERE entity_id = ? ORDER BY event_id";

    private static final String DELETE_ONE =
            "DELETE FROM audit_events WHERE event_id = ?";

    private static final String COUNT = "SELECT COUNT(*) FROM audit_events";

    /**
     * Saves one event and puts the database's new id number back onto the
     * object, so the caller can delete it again if the decision is undone.
     */
    public void save(AuditEvent event) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement =
                     connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, event.getEventType());
            statement.setString(2, event.getEntityTable());
            statement.setString(3, event.getEntityId());
            statement.setString(4, event.getActor());
            statement.setString(5, event.getPayload());
            statement.setString(6, event.getUndoPayload());
            statement.setTimestamp(7, Timestamp.valueOf(event.getCreatedAt()));
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    event.setEventId(keys.getInt(1));
                }
            }
        }
    }

    /** The newest events first. Menu option M7.1 shows the last twenty. */
    public DynamicArray<AuditEvent> findRecent(int howMany) throws SQLException {
        DynamicArray<AuditEvent> found = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_RECENT)) {

            statement.setInt(1, howMany);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(readRow(rows));
                }
            }
        }
        return found;
    }

    /** Everything that ever happened to one request. */
    public DynamicArray<AuditEvent> findForEntity(String entityId) throws SQLException {
        DynamicArray<AuditEvent> found = new DynamicArray<>();

        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_FOR_ENTITY)) {

            statement.setString(1, entityId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    found.add(readRow(rows));
                }
            }
        }
        return found;
    }

    /**
     * Removes an event after its decision has been undone.
     *
     * We could leave the row and add a second event saying "this was undone".
     * Deleting is simpler and matches what the stack does, and the report
     * says which choice we made.
     */
    public void delete(int eventId) throws SQLException {
        try (Connection connection = ConnectionManager.open();
             PreparedStatement statement = connection.prepareStatement(DELETE_ONE)) {

            statement.setInt(1, eventId);
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

    private AuditEvent readRow(ResultSet rows) throws SQLException {
        AuditEvent event = new AuditEvent(
                rows.getString("event_type"),
                rows.getString("entity_table"),
                rows.getString("entity_id"),
                rows.getString("actor"),
                rows.getString("payload"),
                rows.getString("undo_payload"));
        event.setEventId(rows.getInt("event_id"));
        return event;
    }
}
