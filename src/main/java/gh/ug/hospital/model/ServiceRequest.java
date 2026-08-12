package gh.ug.hospital.model;

import gh.ug.hospital.util.TriageScorer;

import java.time.LocalDateTime;

/**
 * Owner: 22367946
 *
 * One job the hospital has to do: move a patient, carry a lab sample,
 * deliver medicine, see someone in clinic. Matches one row of the
 * service_requests table.
 *
 * All 300 of these are made up. They were generated from the hospital's
 * published clinic timetable, the published list of services, the real
 * triage scale and our surveyed location codes. The shape is real. The
 * individual records are not, and no patient information was ever used.
 */
public class ServiceRequest {

    // The seven kinds of job.
    public static final String PATIENT_TRANSFER = "PATIENT_TRANSFER";
    public static final String OUTPATIENT_CONSULT = "OUTPATIENT_CONSULT";
    public static final String LAB_SAMPLE = "LAB_SAMPLE";
    public static final String PHARMACY_DISPATCH = "PHARMACY_DISPATCH";
    public static final String INTER_SITE_REFERRAL = "INTER_SITE_REFERRAL";
    public static final String EMERGENCY_RESPONSE = "EMERGENCY_RESPONSE";
    public static final String SUPPLIES_MOVEMENT = "SUPPLIES_MOVEMENT";

    // Who the patient is. INTERNAL means there is no patient, only supplies.
    public static final String STUDENT = "STUDENT";
    public static final String STAFF = "STAFF";
    public static final String DEPENDENT = "DEPENDENT";
    public static final String COMMUNITY = "COMMUNITY";
    public static final String INTERNAL = "INTERNAL";

    // Where the job has got to.
    public static final String NEW = "NEW";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    public static final String DEFERRED = "DEFERRED";

    private final String requestId;              // Q001 to Q300
    private final String sourceLocationId;
    private final String destinationLocationId;
    private final String category;
    private final int urgency;                   // 5 is red, down to 1 for blue
    private final LocalDateTime timeSubmitted;
    private LocalDateTime deadline;
    private String status;
    private String satsColour;
    private final Integer tewsScore;             // may be null for supplies jobs
    private final String patientClass;
    private final boolean requiresGpReferral;
    private final boolean eligibleAtClinic;
    private String assignedResourceId;

    /** The clinic this job is aimed at, used by the opening-hours check. */
    private final String targetClinic;

    /** How many porter-minutes this job costs. Used by the budget algorithm. */
    private final int porterMinutes;

    public ServiceRequest(String requestId, String sourceLocationId,
                          String destinationLocationId, String category,
                          int urgency, LocalDateTime timeSubmitted,
                          LocalDateTime deadline, String status, String satsColour,
                          Integer tewsScore, String patientClass,
                          boolean requiresGpReferral, boolean eligibleAtClinic,
                          String targetClinic, int porterMinutes) {
        this.requestId = requestId;
        this.sourceLocationId = sourceLocationId;
        this.destinationLocationId = destinationLocationId;
        this.category = category;
        this.urgency = urgency;
        this.timeSubmitted = timeSubmitted;
        this.deadline = deadline;
        this.status = status;
        this.satsColour = satsColour;
        this.tewsScore = tewsScore;
        this.patientClass = patientClass;
        this.requiresGpReferral = requiresGpReferral;
        this.eligibleAtClinic = eligibleAtClinic;
        this.targetClinic = targetClinic;
        this.porterMinutes = porterMinutes;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSourceLocationId() {
        return sourceLocationId;
    }

    public String getDestinationLocationId() {
        return destinationLocationId;
    }

    public String getCategory() {
        return category;
    }

    public int getUrgency() {
        return urgency;
    }

    public LocalDateTime getTimeSubmitted() {
        return timeSubmitted;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSatsColour() {
        return satsColour;
    }

    public void setSatsColour(String satsColour) {
        this.satsColour = satsColour;
    }

    public Integer getTewsScore() {
        return tewsScore;
    }

    public String getPatientClass() {
        return patientClass;
    }

    public boolean requiresGpReferral() {
        return requiresGpReferral;
    }

    public boolean isEligibleAtClinic() {
        return eligibleAtClinic;
    }

    public String getAssignedResourceId() {
        return assignedResourceId;
    }

    public void setAssignedResourceId(String assignedResourceId) {
        this.assignedResourceId = assignedResourceId;
    }

    public String getTargetClinic() {
        return targetClinic;
    }

    public int getPorterMinutes() {
        return porterMinutes;
    }

    /**
     * How much this job is worth if we choose it. The budget algorithm uses
     * this. A more urgent job is worth more.
     */
    public int getValue() {
        return TriageScorer.priorityWeight(satsColour) * 10;
    }

    /** True if the deadline has already gone by at the time given. */
    public boolean isLateAt(LocalDateTime now) {
        return now.isAfter(deadline);
    }

    /** How many minutes are left before the deadline. Negative means late. */
    public long minutesUntilDeadline(LocalDateTime now) {
        return java.time.Duration.between(now, deadline).toMinutes();
    }

    @Override
    public String toString() {
        return requestId + " " + category + " " + satsColour
                + " " + sourceLocationId + "->" + destinationLocationId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ServiceRequest)) {
            return false;
        }
        return requestId.equals(((ServiceRequest) other).requestId);
    }

    @Override
    public int hashCode() {
        return requestId.hashCode();
    }
}
