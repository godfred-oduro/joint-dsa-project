package gh.ug.hospital.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Owner: 22160537
 *
 * Says whether a clinic is open at a given moment, and when it next opens
 * (guide Part 3.3, constraints C1 and C2).
 *
 * These opening hours are published by the hospital. We did not make them up.
 * They matter because a request for the Sickle Cell clinic sent on a Tuesday
 * cannot be dealt with until Friday morning, and that waiting is what gives
 * the scheduling algorithms something real to work on.
 *
 * Note: java.time is not a collection, so using it here does not break
 * constraint C19.
 */
public class ClinicSchedule {

    // The five services that never close.
    public static final String OPD = "OPD";
    public static final String EMERGENCY = "EMERGENCY";
    public static final String PHARMACY = "PHARMACY";
    public static final String LABORATORY = "LABORATORY";
    public static final String AMBULANCE = "AMBULANCE";

    /** Every clinic name the program understands. */
    private static final String[] CLINIC_NAMES = {
            "OPD", "EMERGENCY", "PHARMACY", "LABORATORY", "AMBULANCE",
            "PAEDIATRICS", "OBSTETRICS_GYNAECOLOGY", "INTERNAL_MEDICINE",
            "SICKLE_CELL", "GENERAL_SURGERY", "PUBLIC_HEALTH", "ENT",
            "UROLOGY", "MENTAL_HEALTH", "DERMATOLOGY", "DENTISTRY",
            "OPTOMETRY", "PHYSIOTHERAPY", "DIET_THERAPY"
    };

    public static String[] allClinics() {
        String[] copy = new String[CLINIC_NAMES.length];
        for (int i = 0; i < CLINIC_NAMES.length; i++) {
            copy[i] = CLINIC_NAMES[i];
        }
        return copy;
    }

    /** True if this clinic is one of the five that run 24 hours (C1). */
    public static boolean isOpenAllHours(String clinic) {
        return OPD.equals(clinic)
                || EMERGENCY.equals(clinic)
                || PHARMACY.equals(clinic)
                || LABORATORY.equals(clinic)
                || AMBULANCE.equals(clinic);
    }

    /**
     * Which days this clinic runs. Monday is 1 and Sunday is 7, the same
     * numbering Java uses.
     */
    private static boolean runsOnDay(String clinic, DayOfWeek day) {
        if (isOpenAllHours(clinic)) {
            return true;
        }

        int d = day.getValue();
        boolean weekday = d >= 1 && d <= 5;

        if ("PAEDIATRICS".equals(clinic)) {
            return d == 1 || d == 3;                 // Monday and Wednesday
        }
        if ("OBSTETRICS_GYNAECOLOGY".equals(clinic)) {
            return d >= 1 && d <= 4;                 // Monday to Thursday
        }
        if ("INTERNAL_MEDICINE".equals(clinic)) {
            return d == 2 || d == 5;                 // Tuesday and Friday
        }
        if ("SICKLE_CELL".equals(clinic)) {
            return d == 5;                           // Friday only
        }
        if ("GENERAL_SURGERY".equals(clinic)) {
            return d >= 2 && d <= 4;                 // Tuesday to Thursday
        }
        if ("ENT".equals(clinic)) {
            return d == 2;                           // Tuesday only
        }
        if ("UROLOGY".equals(clinic)) {
            return d == 3 || d == 4;                 // Wednesday and Thursday
        }
        if ("MENTAL_HEALTH".equals(clinic)) {
            return d == 3;                           // Wednesday only
        }
        if ("DERMATOLOGY".equals(clinic)) {
            return d == 4;                           // Thursday only
        }
        // Public Health, Dentistry, Optometry, Physiotherapy, Diet Therapy
        return weekday;
    }

    /** What time this clinic starts seeing patients. */
    private static LocalTime openingTime(String clinic) {
        if (isOpenAllHours(clinic)) {
            return LocalTime.of(0, 0);
        }
        // Three clinics run in the afternoon instead of the morning.
        if ("GENERAL_SURGERY".equals(clinic)
                || "MENTAL_HEALTH".equals(clinic)
                || "DERMATOLOGY".equals(clinic)) {
            return LocalTime.of(14, 0);
        }
        return LocalTime.of(8, 0);
    }

    /** What time this clinic stops seeing patients. */
    private static LocalTime closingTime(String clinic) {
        if (isOpenAllHours(clinic)) {
            return LocalTime.of(23, 59);
        }
        if ("GENERAL_SURGERY".equals(clinic)
                || "MENTAL_HEALTH".equals(clinic)
                || "DERMATOLOGY".equals(clinic)) {
            return LocalTime.of(17, 0);
        }
        return LocalTime.of(14, 0);
    }

    /** True if the clinic is open at that exact moment. */
    public static boolean isOpenAt(String clinic, LocalDateTime when) {
        if (isOpenAllHours(clinic)) {
            return true;
        }
        if (!runsOnDay(clinic, when.getDayOfWeek())) {
            return false;
        }
        LocalTime time = when.toLocalTime();
        return !time.isBefore(openingTime(clinic)) && time.isBefore(closingTime(clinic));
    }

    /**
     * The next moment this clinic opens, at or after the time given.
     *
     * We simply step forward one day at a time and check. The clinic is never
     * shut for more than a week, so this stops quickly.
     */
    public static LocalDateTime nextOpening(String clinic, LocalDateTime from) {
        if (isOpenAt(clinic, from)) {
            return from;
        }

        // Is it opening later today?
        if (runsOnDay(clinic, from.getDayOfWeek())
                && from.toLocalTime().isBefore(openingTime(clinic))) {
            return LocalDateTime.of(from.toLocalDate(), openingTime(clinic));
        }

        // Otherwise look at the following days.
        LocalDateTime day = from.plusDays(1);
        for (int i = 0; i < 8; i++) {
            if (runsOnDay(clinic, day.getDayOfWeek())) {
                return LocalDateTime.of(day.toLocalDate(), openingTime(clinic));
            }
            day = day.plusDays(1);
        }

        throw new IllegalStateException(
                "Could not find an opening time for clinic " + clinic);
    }

    /** A readable line for the console, such as "SICKLE_CELL: Fri 08:00-14:00". */
    public static String describe(String clinic) {
        if (isOpenAllHours(clinic)) {
            return clinic + ": open 24 hours, every day";
        }
        StringBuilder days = new StringBuilder();
        String[] shortNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int d = 1; d <= 7; d++) {
            if (runsOnDay(clinic, DayOfWeek.of(d))) {
                if (days.length() > 0) {
                    days.append(", ");
                }
                days.append(shortNames[d - 1]);
            }
        }
        return clinic + ": " + days + " " + openingTime(clinic) + "-" + closingTime(clinic);
    }

    /**
     * The campus clinic near the Central Cafeteria keeps different hours
     * from the hospital: weekdays 08:00 to 17:00 and Saturday mornings.
     * Outside those hours, everything goes to Emergency (rule BR8).
     */
    public static boolean isCampusClinicOpenAt(LocalDateTime when) {
        int d = when.getDayOfWeek().getValue();
        LocalTime time = when.toLocalTime();

        if (d >= 1 && d <= 5) {
            return !time.isBefore(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(17, 0));
        }
        if (d == 6) {
            return !time.isBefore(LocalTime.of(8, 0)) && time.isBefore(LocalTime.of(12, 0));
        }
        return false;   // Sunday, closed
    }
}
