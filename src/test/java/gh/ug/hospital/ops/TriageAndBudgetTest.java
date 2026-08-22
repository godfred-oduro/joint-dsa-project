package gh.ug.hospital.ops;

import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.util.TriageScorer;
import gh.ug.hospital.exception.StructureException;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Owners: 22224236 (order of treatment), 11102882 (budget),
 *         22372639 (undo)
 *
 * Tests for the order patients are seen in, the porter-minute budget, and
 * taking a decision back.
 */
/**
 * Owner: 22224236
 *
 * Tests for the order patients are seen in and the choice of work within the budget.
 */
public class TriageAndBudgetTest {

    private ServiceRequest request(String id, String colour, int minutesFromNow,
                                   int porterMinutes) {
        LocalDateTime base = LocalDateTime.of(2026, 8, 10, 9, 0);
        LocalDateTime submitted = base.plusMinutes(minutesFromNow);
        return new ServiceRequest(
                id, "L038", "L015", ServiceRequest.PATIENT_TRANSFER,
                TriageScorer.urgencyNumber(colour), submitted,
                submitted.plusMinutes(TriageScorer.targetMinutes(colour)),
                ServiceRequest.NEW, colour, Integer.valueOf(3),
                ServiceRequest.STUDENT, false, false, "OPD", porterMinutes);
    }

    // ---------------- who is seen next ----------------

    @Test
    public void theMostUrgentPatientIsAlwaysSeenFirst() {
        TriageQueue queue = new TriageQueue();
        queue.add(request("Q-green", TriageScorer.GREEN, 0, 10));
        queue.add(request("Q-red", TriageScorer.RED, 5, 10));
        queue.add(request("Q-yellow", TriageScorer.YELLOW, 2, 10));

        // Red arrived last but is seen first.
        assertEquals("Q-red", queue.next().getRequestId());
        assertEquals("Q-yellow", queue.next().getRequestId());
        assertEquals("Q-green", queue.next().getRequestId());
    }

    @Test
    public void twoPatientsOfTheSameColourAreSplitByDeadline() {
        TriageQueue queue = new TriageQueue();
        queue.add(request("Q-later", TriageScorer.YELLOW, 30, 10));
        queue.add(request("Q-sooner", TriageScorer.YELLOW, 0, 10));

        assertEquals("Q-sooner", queue.next().getRequestId());
    }

    @Test
    public void theOrderIsTheSameEveryTimeItIsRun() {
        // Without the final tiebreak on request code, two requests that match
        // on everything else could come out in either order depending on how
        // the heap happened to be arranged. A trace table that changes
        // between runs is useless to an examiner.
        String firstRun = orderOf();
        String secondRun = orderOf();

        assertEquals(firstRun, secondRun);
    }

    private String orderOf() {
        TriageQueue queue = new TriageQueue();
        queue.add(request("Q003", TriageScorer.YELLOW, 0, 10));
        queue.add(request("Q001", TriageScorer.YELLOW, 0, 10));
        queue.add(request("Q002", TriageScorer.YELLOW, 0, 10));

        StringBuilder out = new StringBuilder();
        while (!queue.isEmpty()) {
            out.append(queue.next().getRequestId()).append(" ");
        }
        return out.toString();
    }

    @Test
    public void badInput_askingAnEmptyQueueWhoIsNextFails() {
        final TriageQueue queue = new TriageQueue();

        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                queue.next();
            }
        });
        assertThrows(StructureException.class, new Executable() {
            public void execute() {
                queue.peek();
            }
        });
    }
}
