package gh.ug.hospital.model;

import java.time.LocalDateTime;

/**
 * Owner: 22370298
 *
 * A record of something the program did that changed the data. Matches one
 * row of the audit_events table.
 *
 * Each event carries two things: what the data looks like now, and what it
 * looked like before. That second part is what makes undo possible. The
 * stack holds these events, and undo pops the most recent one and puts the
 * old state back (rule BR14).
 *
 * We write the two states as simple text in JSON shape. We are not using a
 * JSON library, because for a handful of fields plain string building is
 * clearer and adds no dependency.
 */
public class AuditEvent {

    // The kinds of thing that get recorded.
    public static final String DISPATCH = "DISPATCH";
    public static final String REASSIGN = "REASSIGN";
    public static final String CANCEL = "CANCEL";
    public static final String PARAM_CHANGE = "PARAM_CHANGE";
    public static final String LOCATION_ADD = "LOCATION_ADD";
    public static final String AMBULANCE_ESCALATION = "AMBULANCE_ESCALATION";

    private int eventId;
    private final String eventType;
    private final String entityTable;
    private final String entityId;
    private final String actor;
    private final String payload;        // what it looks like now
    private final String undoPayload;    // what it looked like before
    private final LocalDateTime createdAt;

    public AuditEvent(String eventType, String entityTable, String entityId,
                      String actor, String payload, String undoPayload) {
        this.eventType = eventType;
        this.entityTable = entityTable;
        this.entityId = entityId;
        this.actor = actor;
        this.payload = payload;
        this.undoPayload = undoPayload;
        this.createdAt = LocalDateTime.now();
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEntityTable() {
        return entityTable;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getActor() {
        return actor;
    }

    public String getPayload() {
        return payload;
    }

    public String getUndoPayload() {
        return undoPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Builds a small piece of text in JSON shape from pairs of name and value.
     * Call it like: buildState("status", "ASSIGNED", "resource", "RS004")
     */
    public static String buildState(String... nameThenValue) {
        if (nameThenValue.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "buildState needs a value for every name, so an even number of pieces.");
        }
        StringBuilder out = new StringBuilder("{");
        for (int i = 0; i < nameThenValue.length; i += 2) {
            if (i > 0) {
                out.append(",");
            }
            out.append("\"").append(nameThenValue[i]).append("\":");
            String value = nameThenValue[i + 1];
            if (value == null) {
                out.append("null");
            } else {
                out.append("\"").append(value).append("\"");
            }
        }
        out.append("}");
        return out.toString();
    }

    /**
     * Pulls one value back out of a state string. Simple text searching,
     * which is enough for the few fields we store.
     */
    public static String readFromState(String state, String name) {
        if (state == null) {
            return null;
        }
        String marker = "\"" + name + "\":";
        int at = state.indexOf(marker);
        if (at < 0) {
            return null;
        }
        int valueStart = at + marker.length();
        if (state.startsWith("null", valueStart)) {
            return null;
        }
        int openQuote = state.indexOf('"', valueStart);
        if (openQuote < 0) {
            return null;
        }
        int closeQuote = state.indexOf('"', openQuote + 1);
        if (closeQuote < 0) {
            return null;
        }
        return state.substring(openQuote + 1, closeQuote);
    }

    @Override
    public String toString() {
        return eventType + " on " + entityTable + " " + entityId
                + " at " + createdAt;
    }
}
