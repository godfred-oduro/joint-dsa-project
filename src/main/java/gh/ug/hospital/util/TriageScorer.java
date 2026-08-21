package gh.ug.hospital.util;

/**
 * Owner: 11102882
 *
 * Turns a TEWS score into a triage colour, and gives the target time for
 * each colour (guide Part 4 and rule BR6).
 *
 * TEWS stands for Triage Early Warning Score. Nurses work it out from seven
 * observations: how well the patient can move, heart rate, breathing rate,
 * blood pressure, temperature, how awake they are, and whether there was an
 * injury. It runs from 0 to 17.
 *
 * The scale itself is the South African Triage Scale, which Ghanaian
 * emergency units use. We did not invent these numbers.
 */
public class TriageScorer {

    // The five colours, written the same way everywhere in the project.
    public static final String RED = "RED";
    public static final String ORANGE = "ORANGE";
    public static final String YELLOW = "YELLOW";
    public static final String GREEN = "GREEN";
    public static final String BLUE = "BLUE";

    /**
     * Turns a TEWS score into a colour.
     *
     *   0, 1, 2      -> green
     *   3, 4         -> yellow
     *   5, 6         -> orange
     *   7 or more    -> red
     *
     * Blue is never produced here. Blue means dead on arrival, which a
     * score cannot tell you.
     */
    public static String colourFromTews(int tews) {
        if (tews < 0 || tews > 17) {
            throw new IllegalArgumentException(
                    "A TEWS score runs from 0 to 17, but got " + tews);
        }
        if (tews >= 7) {
            return RED;
        }
        if (tews >= 5) {
            return ORANGE;
        }
        if (tews >= 3) {
            return YELLOW;
        }
        return GREEN;
    }

    /**
     * How many minutes the patient should wait at most, by colour.
     * These come straight from the published scale.
     */
    public static int targetMinutes(String colour) {
        if (RED.equals(colour)) {
            return 0;
        }
        if (ORANGE.equals(colour)) {
            return 10;
        }
        if (YELLOW.equals(colour)) {
            return 60;
        }
        if (GREEN.equals(colour)) {
            return 240;
        }
        if (BLUE.equals(colour)) {
            return 120;
        }
        throw new IllegalArgumentException("Unknown triage colour: " + colour);
    }

    /**
     * The number the database stores, because the project template uses a
     * number rather than a word. Red is 5, down to blue at 1.
     */
    public static int urgencyNumber(String colour) {
        if (RED.equals(colour)) {
            return 5;
        }
        if (ORANGE.equals(colour)) {
            return 4;
        }
        if (YELLOW.equals(colour)) {
            return 3;
        }
        if (GREEN.equals(colour)) {
            return 2;
        }
        if (BLUE.equals(colour)) {
            return 1;
        }
        throw new IllegalArgumentException("Unknown triage colour: " + colour);
    }

    /** The other way round: 5 becomes RED, 1 becomes BLUE. */
    public static String colourFromUrgency(int urgency) {
        if (urgency == 5) {
            return RED;
        }
        if (urgency == 4) {
            return ORANGE;
        }
        if (urgency == 3) {
            return YELLOW;
        }
        if (urgency == 2) {
            return GREEN;
        }
        if (urgency == 1) {
            return BLUE;
        }
        throw new IllegalArgumentException(
                "Urgency runs from 1 to 5, but got " + urgency);
    }

    /**
     * How much weight this colour carries when we order the queue.
     * Red gets an extra push from setting P5, which came from our index
     * numbers. Everything else uses its plain urgency number.
     */
    public static int priorityWeight(String colour) {
        int base = urgencyNumber(colour);
        if (RED.equals(colour)) {
            return base * Parameters.getRedPriorityMultiplier();
        }
        return base;
    }

    /**
     * The 32 warning signs in the real scale can push a patient up a colour
     * even when their score is low. We model that as a simple flag.
     * This is what makes our greedy counterexample work (BR13).
     */
    public static String applyWarningSign(String colour, boolean warningSignPresent) {
        if (!warningSignPresent) {
            return colour;
        }
        if (GREEN.equals(colour)) {
            return YELLOW;
        }
        if (YELLOW.equals(colour)) {
            return ORANGE;
        }
        if (ORANGE.equals(colour)) {
            return RED;
        }
        // Red cannot go higher, and blue is not raised.
        return colour;
    }
}
