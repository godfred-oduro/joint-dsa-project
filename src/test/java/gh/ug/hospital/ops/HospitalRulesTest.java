package gh.ug.hospital.ops;

import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.model.ServiceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owners: 22160537 (waiting for clinics), 22305896 (who goes where)
 *
 * Tests for the hospital's own rules, not for the data structures.
 *
 * These are the tests that make this a hospital system rather than a
 * structures exercise. Every one of them checks something the hospital
 * actually publishes about how it works.
 */
/**
 * Owner: 22160537
 *
 * Tests for the clinic timetable, the referral order and who may be seen where.
 */
public class HospitalRulesTest {

    /** Builds a request with just enough filled in for these tests. */
    private ServiceRequest request(String id, String colour, String patientClass,
                                   String clinic, boolean eligibleAtClinic,
                                   LocalDateTime submitted) {
        return new ServiceRequest(
                id, "L038", "L015", ServiceRequest.OUTPATIENT_CONSULT,
                3, submitted, submitted.plusMinutes(60), ServiceRequest.NEW,
                colour, Integer.valueOf(3), patientClass,
                false, eligibleAtClinic, clinic, 15);
    }

    // ---------------- clinics that are only open on some days ----------------

    @Test
    public void sickleCellRequestSentOnTuesdayWaitsUntilFriday() {
        // The Sickle Cell clinic runs on Friday mornings only. That is
        // published by the hospital. A request sent on Tuesday cannot be
        // dealt with until Friday, and that three day wait is exactly what
        // gives the scheduler something real to work with.
        LocalDateTime tuesday = LocalDateTime.of(2026, 8, 11, 10, 0);
        ServiceRequest sickleCell = request("Q001", "YELLOW",
                ServiceRequest.STUDENT, "SICKLE_CELL", false, tuesday);

        LocalDateTime servedAt = ClinicDeferral.resolve(sickleCell, tuesday);

        assertEquals(ServiceRequest.DEFERRED, sickleCell.getStatus());
        assertEquals(5, servedAt.getDayOfWeek().getValue(), "should wait for Friday");
        assertEquals(8, servedAt.getHour());
    }

    @Test
    public void aClinicOpenTodayServesTheRequestStraightAway() {
        // Dentistry runs Monday to Friday, mornings.
        LocalDateTime mondayMorning = LocalDateTime.of(2026, 8, 10, 9, 30);
        ServiceRequest dental = request("Q002", "GREEN",
                ServiceRequest.STAFF, "DENTISTRY", false, mondayMorning);

        LocalDateTime servedAt = ClinicDeferral.resolve(dental, mondayMorning);

        assertEquals(mondayMorning, servedAt);
        assertEquals(ServiceRequest.NEW, dental.getStatus());
    }

    @Test
    public void atTenAtNightThePharmacyIsOpenButDermatologyIsNot() {
        // Five services run 24 hours: OPD, Emergency, Pharmacy, Laboratory
        // and Ambulance. Everything else waits for morning.
        LocalDateTime lateEvening = LocalDateTime.of(2026, 8, 10, 22, 0);

        final ServiceRequest pharmacy = request("Q003", "YELLOW",
                ServiceRequest.STUDENT, "PHARMACY", false, lateEvening);
        final ServiceRequest dermatology = request("Q004", "GREEN",
                ServiceRequest.STUDENT, "DERMATOLOGY", false, lateEvening);

        // The pharmacy is fine.
        ClinicDeferral.requireOpen(pharmacy, lateEvening);

        // Dermatology is not.
        assertThrows(DomainException.class, new Executable() {
            public void execute() {
                ClinicDeferral.requireOpen(dermatology, lateEvening);
            }
        });
    }

    @Test
    public void badInput_aRequestForAClosedClinicIsRefused() {
        // The Sickle Cell clinic runs on Friday mornings only. A request sent
        // on a Tuesday cannot be dealt with, and the rule has to say so
        // rather than pretend the clinic is open.
        final ServiceRequest tuesdayRequest = new ServiceRequest(
                "Q900", "L001", "L015", ServiceRequest.OUTPATIENT_CONSULT,
                2, LocalDateTime.of(2026, 8, 11, 9, 0),
                LocalDateTime.of(2026, 8, 11, 13, 0),
                ServiceRequest.NEW, "GREEN", Integer.valueOf(1),
                ServiceRequest.STUDENT, true, false, "SICKLE_CELL", 10);

        assertThrows(DomainException.class, new Executable() {
            public void execute() {
                ClinicDeferral.requireOpen(tuesdayRequest,
                        LocalDateTime.of(2026, 8, 11, 9, 0));
            }
        });
    }
}
