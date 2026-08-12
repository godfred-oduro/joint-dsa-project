package gh.ug.hospital.model;

/**
 * Owner: 22392509
 *
 * One path or small road between two places. Matches one row of the roads
 * table.
 *
 * Paths work in both directions, so we store each one once and treat it as
 * two-way when we build the graph.
 *
 * The number the routing algorithm actually uses is weightedCostMinutes,
 * which is the travel time multiplied by the condition weight. An unpaved
 * path carries the 1.9 penalty from setting P3, so a shorter rough path can
 * lose to a longer paved one. That is the point.
 *
 * The two observed times are empty for now. They get filled in from the
 * stopwatch readings taken during the visit to the hospital.
 */
public class Road {

    private final String roadId;             // RD001 to RD116
    private final String fromLocationId;
    private final String toLocationId;
    private final double distanceKm;
    private final double travelTimeMinutes;
    private final double conditionWeight;    // 1.0 normally, up to 2.185
    private final String roadType;           // path, corridor, access_road, service_road, public_road
    private final String surface;            // paved or unpaved
    private final String covered;            // Y or N
    private final double distanceMetres;
    private final double walkTimeMinutes;
    private final double weightedCostMinutes;
    private final String isAccessible;       // Y or N
    private final String congestionZone;     // OPD_PEAK or NORMAL

    private Double observedTime0900;         // null until someone measures it
    private Double observedTime1500;

    private final String source;
    private final String confidence;
    private final String notes;

    public Road(String roadId, String fromLocationId, String toLocationId,
                double distanceKm, double travelTimeMinutes, double conditionWeight,
                String roadType, String surface, String covered,
                double distanceMetres, double walkTimeMinutes, double weightedCostMinutes,
                String isAccessible, String congestionZone,
                String source, String confidence, String notes) {
        this.roadId = roadId;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distanceKm = distanceKm;
        this.travelTimeMinutes = travelTimeMinutes;
        this.conditionWeight = conditionWeight;
        this.roadType = roadType;
        this.surface = surface;
        this.covered = covered;
        this.distanceMetres = distanceMetres;
        this.walkTimeMinutes = walkTimeMinutes;
        this.weightedCostMinutes = weightedCostMinutes;
        this.isAccessible = isAccessible;
        this.congestionZone = congestionZone;
        this.source = source;
        this.confidence = confidence;
        this.notes = notes;
    }

    public String getRoadId() {
        return roadId;
    }

    public String getFromLocationId() {
        return fromLocationId;
    }

    public String getToLocationId() {
        return toLocationId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public double getConditionWeight() {
        return conditionWeight;
    }

    public String getRoadType() {
        return roadType;
    }

    public String getSurface() {
        return surface;
    }

    public String getCovered() {
        return covered;
    }

    public double getDistanceMetres() {
        return distanceMetres;
    }

    public double getWalkTimeMinutes() {
        return walkTimeMinutes;
    }

    /** The number Dijkstra and the spanning tree algorithms use. */
    public double getWeightedCostMinutes() {
        return weightedCostMinutes;
    }

    public String getIsAccessible() {
        return isAccessible;
    }

    public String getCongestionZone() {
        return congestionZone;
    }

    public boolean isUnpaved() {
        return "unpaved".equals(surface);
    }

    public boolean isCovered() {
        return "Y".equals(covered);
    }

    /** True if this path gets busy in the morning. */
    public boolean isBusyAtPeak() {
        return "OPD_PEAK".equals(congestionZone);
    }

    /** True if a vehicle uses this rather than a person on foot. */
    public boolean isVehicleRoad() {
        return "access_road".equals(roadType)
                || "service_road".equals(roadType)
                || "public_road".equals(roadType);
    }

    public Double getObservedTime0900() {
        return observedTime0900;
    }

    public void setObservedTime0900(Double minutes) {
        this.observedTime0900 = minutes;
    }

    public Double getObservedTime1500() {
        return observedTime1500;
    }

    public void setObservedTime1500(Double minutes) {
        this.observedTime1500 = minutes;
    }

    /** True once someone has actually timed this path on site. */
    public boolean hasBeenMeasured() {
        return observedTime0900 != null || observedTime1500 != null;
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

    /** Given one end, hands back the other. Useful when walking the graph. */
    public String otherEnd(String oneEnd) {
        if (fromLocationId.equals(oneEnd)) {
            return toLocationId;
        }
        if (toLocationId.equals(oneEnd)) {
            return fromLocationId;
        }
        throw new IllegalArgumentException(
                oneEnd + " is not an end of road " + roadId);
    }

    @Override
    public String toString() {
        return roadId + " " + fromLocationId + "-" + toLocationId
                + " " + String.format("%.2f", weightedCostMinutes) + " min";
    }
}
