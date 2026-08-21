package gh.ug.hospital.util;

import gh.ug.hospital.exception.DomainException;

/**
 * Owner: 22248029
 *
 * The five settings that come from our index numbers (guide Part 6.7).
 *
 * Nobody writes 11959 or 1.9 into their own file. Everyone reads the values
 * from here, because menu option M8 lets an examiner change them while the
 * program is running, and that only works if there is one place holding them.
 *
 * Base figures from all 16 index numbers:
 *   count            = 16
 *   sum              = 345,271,958
 *   lowest           = 11,102,882
 *   highest          = 22,399,601
 *   average (floor)  = 21,579,497
 *   sum of digits    = 476
 *
 * The group was 17 at first. When it became 16, these figures were worked
 * out again from the remaining numbers, and two of the five settings moved:
 * the hash table size went from 7669 to 11959, and the porter-minute budget
 * from 532 to 497. The other three came out the same by chance. Say this in
 * the report rather than quietly changing the numbers.
 */
public final class Parameters {

    /** Nobody should create one of these. It is a box of settings, not an object. */
    private Parameters() {
    }

    // ---- the working values, which M8 can change ----

    private static int hashTableSize = 11959;
    private static long randomSeed = 1110288222015166L;
    private static double roughRoadPenalty = 1.9;
    private static int porterMinuteBudget = 497;
    private static int redPriorityMultiplier = 2;

    // ---- the original values, so M8 can put things back ----

    public static final int DEFAULT_HASH_TABLE_SIZE = 11959;
    public static final long DEFAULT_RANDOM_SEED = 1110288222015166L;
    public static final double DEFAULT_ROUGH_ROAD_PENALTY = 1.9;
    public static final int DEFAULT_PORTER_MINUTE_BUDGET = 497;
    public static final int DEFAULT_RED_PRIORITY_MULTIPLIER = 2;

    // ---- P1: hash table size ----

    public static int getHashTableSize() {
        return hashTableSize;
    }

    public static void setHashTableSize(int value) {
        // A hash table smaller than 2 makes no sense, and above 100000 we are
        // just wasting memory for a project this size.
        if (value < 2 || value > 100000) {
            throw new DomainException(
                    "P1 hash table size must be between 2 and 100000, but got " + value);
        }
        hashTableSize = value;
    }

    // ---- P2: random seed ----

    public static long getRandomSeed() {
        return randomSeed;
    }

    public static void setRandomSeed(long value) {
        randomSeed = value;
    }

    // ---- P3: rough road penalty, applied to unpaved paths ----

    public static double getRoughRoadPenalty() {
        return roughRoadPenalty;
    }

    public static void setRoughRoadPenalty(double value) {
        // A value of 1.0 would be no penalty at all, which is the mistake we
        // caught in the first formula. See guide Part 6.7.
        if (value < 1.0 || value > 5.0) {
            throw new DomainException(
                    "P3 rough road penalty must be between 1.0 and 5.0, but got " + value);
        }
        roughRoadPenalty = value;
    }

    // ---- P4: how many porter-minutes the budget algorithm may spend ----

    public static int getPorterMinuteBudget() {
        return porterMinuteBudget;
    }

    public static void setPorterMinuteBudget(int value) {
        if (value < 1 || value > 10000) {
            throw new DomainException(
                    "P4 porter-minute budget must be between 1 and 10000, but got " + value);
        }
        porterMinuteBudget = value;
    }

    // ---- P5: how much extra weight a red case carries ----

    public static int getRedPriorityMultiplier() {
        return redPriorityMultiplier;
    }

    public static void setRedPriorityMultiplier(int value) {
        if (value < 1 || value > 10) {
            throw new DomainException(
                    "P5 red priority multiplier must be between 1 and 10, but got " + value);
        }
        redPriorityMultiplier = value;
    }

    /** Puts all five settings back to the values from our index numbers. */
    public static void resetAll() {
        hashTableSize = DEFAULT_HASH_TABLE_SIZE;
        randomSeed = DEFAULT_RANDOM_SEED;
        roughRoadPenalty = DEFAULT_ROUGH_ROAD_PENALTY;
        porterMinuteBudget = DEFAULT_PORTER_MINUTE_BUDGET;
        redPriorityMultiplier = DEFAULT_RED_PRIORITY_MULTIPLIER;
    }

    /** A short summary for the M8 menu screen. */
    public static String describe() {
        StringBuilder out = new StringBuilder();
        out.append("P1  hash table size        = ").append(hashTableSize).append("\n");
        out.append("P2  random seed            = ").append(randomSeed).append("\n");
        out.append("P3  rough road penalty     = ").append(roughRoadPenalty).append("\n");
        out.append("P4  porter-minute budget   = ").append(porterMinuteBudget).append("\n");
        out.append("P5  red priority multiplier= ").append(redPriorityMultiplier);
        return out.toString();
    }
}
