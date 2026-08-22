package gh.ug.hospital.ops;

import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.ClinicSchedule;
import gh.ug.hospital.util.TriageScorer;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Owner: 22160537
 *
 * Handles requests that arrive when the clinic is shut. Rules BR3 and BR4.
 *
 * The hospital publishes its clinic timetable, and it is uneven. The Sickle
 * Cell clinic runs on Friday mornings only. ENT runs on Tuesdays. Dermatology
 * runs Thursday afternoons. So a request sent on Tuesday for Sickle Cell has
 * to wait three days, and that waiting is what gives the scheduler something
 * real to work with.
 *
 * One detail matters more than it looks. When a request is put off, the
 * deadline is still worked out from when it was FIRST sent, not from when
 * the clinic opens. If we reset the clock, a patient could wait three days
 * and still appear to be on time, which would be nonsense. The waiting has
 * to count against them.
 */
public class ClinicDeferral {

    /** The five services that never close. */
    public static boolean isAlwaysOpen(String clinic) {
        return ClinicSchedule.isOpenAllHours(clinic);
    }

    /**
     * Checks whether a request can be dealt with now, and puts it off if not.
     *
     * @return the moment the request can actually be served
     */
    public static LocalDateTime resolve(ServiceRequest request, LocalDateTime now) {
        String clinic = request.getTargetClinic();

        // Supplies jobs and internal transfers have no clinic, so nothing to check.
        if (clinic == null || clinic.isEmpty()) {
            return now;
        }

        // Rule BR4: overnight, only the five 24-hour services may be targeted.
        if (isNightTime(now) && !isAlwaysOpen(clinic)) {
            LocalDateTime opensAt = ClinicSchedule.nextOpening(clinic, now);
            request.setStatus(ServiceRequest.DEFERRED);
            return opensAt;
        }

        // Rule BR3: if the clinic is shut, wait until it opens.
        if (!ClinicSchedule.isOpenAt(clinic, now)) {
            LocalDateTime opensAt = ClinicSchedule.nextOpening(clinic, now);
            request.setStatus(ServiceRequest.DEFERRED);
            return opensAt;
        }

        return now;
    }

    /**
     * The same check, but it complains instead of quietly putting the request
     * off. The menu uses this so an examiner can see the rule biting.
     */
    public static void requireOpen(ServiceRequest request, LocalDateTime now) {
        String clinic = request.getTargetClinic();
        if (clinic == null || clinic.isEmpty()) {
            return;
        }

        if (isNightTime(now) && !isAlwaysOpen(clinic)) {
            throw new DomainException(
                    request.getRequestId() + ": " + clinic
                            + " is not a 24-hour service, and it is now " + now.toLocalTime()
                            + ". Only OPD, Emergency, Pharmacy, Laboratory and Ambulance "
                            + "run overnight. Next opening is "
                            + ClinicSchedule.nextOpening(clinic, now) + ".");
        }

        if (!ClinicSchedule.isOpenAt(clinic, now)) {
            throw new DomainException(
                    request.getRequestId() + ": " + clinic + " is closed at " + now
                            + ". It next opens " + ClinicSchedule.nextOpening(clinic, now) + ".");
        }
    }

    /** True between 20:00 and 08:00. */
    public static boolean isNightTime(LocalDateTime when) {
        LocalTime time = when.toLocalTime();
        return time.isAfter(LocalTime.of(19, 59)) || time.isBefore(LocalTime.of(8, 0));
    }

    /**
     * Sets the deadline from when the request was first sent, whatever
     * happens afterwards. This is rule BR2.
     */
    public static void setDeadlineFromSubmission(ServiceRequest request) {
        int target = TriageScorer.targetMinutes(request.getSatsColour());
        request.setDeadline(request.getTimeSubmitted().plusMinutes(target));
    }

    /**
     * How long a request had to wait because its clinic was shut. Useful for
     * the report, since it shows the timetable actually doing something.
     */
    public static long waitingMinutes(ServiceRequest request, LocalDateTime now) {
        LocalDateTime servedAt = resolve(request, now);
        return java.time.Duration.between(now, servedAt).toMinutes();
    }
}
