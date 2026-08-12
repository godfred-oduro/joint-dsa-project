package gh.ug.hospital.model;

import java.time.LocalTime;

/**
 * Owner: 22395395
 *
 * Something or someone that can be given a job: a porter, a nurse, a
 * wheelchair, a trolley, an ambulance. Matches one row of the resources
 * table.
 *
 * Every resource here is marked with confidence D, because nobody has
 * counted them yet. The numbers were worked out from the hospital's
 * published staff total of 302, of whom 232 deal with patients directly.
 * They are stated estimates, not measurements, and the report says so.
 */
public class Resource {

    // The kinds of resource the program understands.
    public static final String PORTER = "PORTER";
    public static final String NURSE = "NURSE";
    public static final String MIDWIFE = "MIDWIFE";
    public static final String DOCTOR = "DOCTOR";
    public static final String WHEELCHAIR = "WHEELCHAIR";
    public static final String TROLLEY = "TROLLEY";
    public static final String AMBULANCE = "AMBULANCE";
    public static final String SUPPLY_TROLLEY = "SUPPLY_TROLLEY";
    public static final String OXYGEN_CYLINDER = "OXYGEN_CYLINDER";

    // What state a resource can be in.
    public static final String AVAILABLE = "AVAILABLE";
    public static final String BUSY = "BUSY";
    public static final String OFF_DUTY = "OFF_DUTY";
    public static final String MAINTENANCE = "MAINTENANCE";

    // Which shift it works.
    public static final String MORNING = "MORNING";
    public static final String AFTERNOON = "AFTERNOON";
    public static final String NIGHT = "NIGHT";
    public static final String ALL_SHIFTS = "ALL";

    private final String resourceId;         // RS001 to RS034
    private final String resourceType;
    private final String homeLocationId;     // where it normally sits
    private final int capacity;              // how many jobs at once
    private String availabilityStatus;
    private final String shift;
    private final LocalTime availableFrom;
    private final LocalTime availableTo;
    private final String source;
    private final String confidence;

    /** How many jobs it is handling right now. */
    private int currentLoad;

    public Resource(String resourceId, String resourceType, String homeLocationId,
                    int capacity, String availabilityStatus, String shift,
                    LocalTime availableFrom, LocalTime availableTo,
                    String source, String confidence) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.homeLocationId = homeLocationId;
        this.capacity = capacity;
        this.availabilityStatus = availabilityStatus;
        this.shift = shift;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
        this.source = source;
        this.confidence = confidence;
        this.currentLoad = 0;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getHomeLocationId() {
        return homeLocationId;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getShift() {
        return shift;
    }

    public LocalTime getAvailableFrom() {
        return availableFrom;
    }

    public LocalTime getAvailableTo() {
        return availableTo;
    }

    public String getSource() {
        return source;
    }

    public String getConfidence() {
        return confidence;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    /** True if it is free and has room for another job. Rules C9 and C10. */
    public boolean canTakeWork() {
        return AVAILABLE.equals(availabilityStatus) && currentLoad < capacity;
    }

    /** Gives this resource one more job. */
    public void takeJob() {
        if (!canTakeWork()) {
            throw new IllegalStateException(
                    resourceId + " cannot take another job. Status is "
                            + availabilityStatus + " and load is "
                            + currentLoad + " of " + capacity + ".");
        }
        currentLoad++;
        if (currentLoad >= capacity) {
            availabilityStatus = BUSY;
        }
    }

    /** Finishes one job and frees the resource up again if it has room. */
    public void finishJob() {
        if (currentLoad > 0) {
            currentLoad--;
        }
        if (currentLoad < capacity && BUSY.equals(availabilityStatus)) {
            availabilityStatus = AVAILABLE;
        }
    }

    /** True if this resource is on duty at that time of day. */
    public boolean isOnShiftAt(LocalTime time) {
        if (ALL_SHIFTS.equals(shift)) {
            return true;
        }
        if (availableFrom == null || availableTo == null) {
            return true;
        }
        // The night shift runs past midnight, so its start is later than its end.
        if (availableFrom.isAfter(availableTo)) {
            return !time.isBefore(availableFrom) || time.isBefore(availableTo);
        }
        return !time.isBefore(availableFrom) && time.isBefore(availableTo);
    }

    @Override
    public String toString() {
        return resourceId + " " + resourceType + " at " + homeLocationId
                + " (" + availabilityStatus + " " + currentLoad + "/" + capacity + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Resource)) {
            return false;
        }
        return resourceId.equals(((Resource) other).resourceId);
    }

    @Override
    public int hashCode() {
        return resourceId.hashCode();
    }
}
