package gh.ug.hospital.app;

import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.util.Parameters;

/**
 * Owner: 22248029
 *
 * Menu option 8. Shows the five settings and lets them be changed while the
 * program is running.
 *
 * This exists for the oral defence. The brief warns that an examiner may ask
 * for a rule to be changed and the program run again on the spot. Without
 * this menu that would mean editing code and recompiling in front of them.
 * With it, it is one menu choice.
 *
 * Every change is written into the audit log, so it can also be undone.
 */
public class ParameterMenu {

    private final Session session;

    private static final String[] OPTIONS = {
            "P1  hash table size",
            "P2  random seed",
            "P3  rough road penalty",
            "P4  porter-minute budget",
            "P5  red priority multiplier",
            "Put everything back to the values from our index numbers"
    };

    public ParameterMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            Console.heading("8. SETTINGS");
            Console.say(Parameters.describe());
            Console.blank();
            Console.say("These five came from the group's index numbers. The formulas");
            Console.say("are in the README. Changing one here changes it everywhere,");
            Console.say("because nothing writes these numbers into its own file.");
            Console.blank();
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Change which", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }

            try {
                change(choice);
            } catch (DomainException refused) {
                Console.problem(refused.getMessage());
            }
            Console.pause();
        }
    }

    private void change(int choice) {
        if (choice == 1) {
            int old = Parameters.getHashTableSize();
            int fresh = (int) Console.askLong("New hash table size (2 to 100000)");
            Parameters.setHashTableSize(fresh);
            record("P1", String.valueOf(old), String.valueOf(fresh));
            Console.say("P1 changed from " + old + " to " + fresh + ".");
            Console.say("This affects every map and set built from now on, and");
            Console.say("experiment E3 in particular.");

        } else if (choice == 2) {
            long old = Parameters.getRandomSeed();
            long fresh = Console.askLong("New random seed");
            Parameters.setRandomSeed(fresh);
            record("P2", String.valueOf(old), String.valueOf(fresh));
            Console.say("P2 changed. The timing experiments will now use different");
            Console.say("test data, so results are no longer comparable with earlier runs.");

        } else if (choice == 3) {
            double old = Parameters.getRoughRoadPenalty();
            double fresh = Console.askDecimal("New rough road penalty (1.0 to 5.0)");
            Parameters.setRoughRoadPenalty(fresh);
            record("P3", String.valueOf(old), String.valueOf(fresh));
            Console.say("P3 changed from " + old + " to " + fresh + ".");
            Console.say("Unpaved paths now cost " + fresh + " times as much, so the");
            Console.say("shortest route may well come out different. Try option 4.3.");

        } else if (choice == 4) {
            int old = Parameters.getPorterMinuteBudget();
            int fresh = (int) Console.askLong("New porter-minute budget (1 to 10000)");
            Parameters.setPorterMinuteBudget(fresh);
            record("P4", String.valueOf(old), String.valueOf(fresh));
            Console.say("P4 changed from " + old + " to " + fresh + ".");
            Console.say("The budget algorithm will now choose a different set of jobs.");

        } else if (choice == 5) {
            int old = Parameters.getRedPriorityMultiplier();
            int fresh = (int) Console.askLong("New red priority multiplier (1 to 10)");
            Parameters.setRedPriorityMultiplier(fresh);
            record("P5", String.valueOf(old), String.valueOf(fresh));
            Console.say("P5 changed from " + old + " to " + fresh + ".");
            Console.say("Red cases now push harder in the queue. Try option 4.1.");

        } else if (choice == 6) {
            Parameters.resetAll();
            Console.say("All five settings are back to the values worked out from");
            Console.say("the group's index numbers.");
        }
    }

    /** Writes the change into the audit log so it can be undone. */
    private void record(String code, String oldValue, String newValue) {
        session.getUndoService().recordParameterChange(code, oldValue, newValue, "console");
    }
}
