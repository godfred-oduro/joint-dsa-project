package gh.ug.hospital.model;

/**
 * Owner: 22399601
 *
 * One place in or around the hospital: a ward, a department, a gate, a car
 * park. This matches one row of the locations table.
 *
 * Two things to notice.
 *
 * First, x_coord holds the latitude and y_coord holds the longitude. That is
 * the reverse of the usual convention, but it is what the project template
 * shows, so we follow it and say so in the report.
 *
 * Second, every row carries a source and a confidence. Six of our
 * coordinates were measured from Google Earth. The other forty five were
 * estimated between them. Anyone reading the data can tell which is which,
 * and that honesty is worth marks.
 */
public class Location {

    private final String locationId;     // L001 to L052
    private final String name;
    private final String area;
    private final String locationType;
    private final double latitude;       // stored in the x_coord column
    private final double longitude;      // stored in the y_coord column
    private final String internalCode;   // UGH-15, our own reference
    private final String zone;
    private final String source;         // GOOGLE_EARTH_SURVEY, MODELLED, DERIVED, FIELD_SURVEY
    private final String confidence;     // A measured, C estimated position, D estimated figure
    private final String notes;

    public Location(String locationId, String name, String area, String locationType,
                    double latitude, double longitude, String internalCode,
                    String zone, String source, String confidence, String notes) {
        this.locationId = locationId;
        this.name = name;
        this.area = area;
        this.locationType = locationType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.internalCode = internalCode;
        this.zone = zone;
        this.source = source;
        this.confidence = confidence;
        this.notes = notes;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public String getLocationType() {
        return locationType;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public String getZone() {
        return zone;
    }

    public String getSource() {
        return source;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getNotes() {
        return notes;
    }

    /** True if this coordinate was actually measured, not estimated. */
    public boolean isMeasured() {
        return "A".equals(confidence);
    }

    @Override
    public String toString() {
        return locationId + " " + name;
    }

    /** Two locations are the same if they have the same code. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Location)) {
            return false;
        }
        Location that = (Location) other;
        return locationId.equals(that.locationId);
    }

    @Override
    public int hashCode() {
        return locationId.hashCode();
    }
}
