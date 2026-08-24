package gh.ug.hospital.app;

import java.util.Scanner;

/**
 * Owner: 22248029
 *
 * Reads what the user types and prints things back neatly.
 *
 * Every menu uses this, so the rules from the guide are in one place rather
 * than repeated ten times:
 *
 *   typing 0 always goes back
 *   typing letters where a number belongs simply asks again
 *   pressing enter with nothing typed asks again
 *   the program never stops because of bad input
 *
 * java.util.Scanner is a keyboard reader, not a collection, so using it here
 * does not break constraint C19. It is also only used in the app package.
 */
public class Console {

    private static final Scanner KEYBOARD = new Scanner(System.in);

    /** Marks the value returned when the user wants to go back. */
    public static final int GO_BACK = 0;

    /** Prints a heading so the user always knows where they are. */
    public static void heading(String title) {
        System.out.println();
        System.out.println("-----------------------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("-----------------------------------------------------------------");
    }

    public static void say(String line) {
        System.out.println(line);
    }

    public static void blank() {
        System.out.println();
    }

    /** Prints a problem in a way that stands out but does not look like a crash. */
    public static void problem(String message) {
        System.out.println();
        System.out.println("  >> " + message);
        System.out.println();
    }

    /**
     * Asks for a number between two limits.
     *
     * Keeps asking until it gets one. Typing 0 gives GO_BACK.
     */
    public static int askNumber(String question, int lowest, int highest) {
        while (true) {
            System.out.print(question + " (" + lowest + "-" + highest + ", 0 to go back): ");
            String typed = KEYBOARD.nextLine().trim();

            if (typed.isEmpty()) {
                System.out.println("  Nothing was typed. Try again.");
                continue;
            }

            int value;
            try {
                value = Integer.parseInt(typed);
            } catch (NumberFormatException notANumber) {
                System.out.println("  That is not a number. Try again.");
                continue;
            }

            if (value == GO_BACK) {
                return GO_BACK;
            }
            if (value < lowest || value > highest) {
                System.out.println("  Please choose between " + lowest
                        + " and " + highest + ".");
                continue;
            }
            return value;
        }
    }

    /** Asks for a whole number with no upper limit worth enforcing. */
    public static long askLong(String question) {
        while (true) {
            System.out.print(question + ": ");
            String typed = KEYBOARD.nextLine().trim();
            try {
                return Long.parseLong(typed);
            } catch (NumberFormatException notANumber) {
                System.out.println("  That is not a whole number. Try again.");
            }
        }
    }

    /** Asks for a number that may have a decimal point. */
    public static double askDecimal(String question) {
        while (true) {
            System.out.print(question + ": ");
            String typed = KEYBOARD.nextLine().trim();
            try {
                return Double.parseDouble(typed);
            } catch (NumberFormatException notANumber) {
                System.out.println("  That is not a number. Try again.");
            }
        }
    }

    /** Asks for some text. Empty input is allowed and comes back empty. */
    public static String askText(String question) {
        System.out.print(question + ": ");
        return KEYBOARD.nextLine().trim();
    }

    /** Asks a yes or no question. */
    public static boolean askYesNo(String question) {
        while (true) {
            System.out.print(question + " (y/n): ");
            String typed = KEYBOARD.nextLine().trim().toLowerCase();
            if (typed.startsWith("y")) {
                return true;
            }
            if (typed.startsWith("n")) {
                return false;
            }
            System.out.println("  Please type y or n.");
        }
    }

    /** Waits for the user to read something before the screen moves on. */
    public static void pause() {
        System.out.print("  Press enter to continue: ");
        KEYBOARD.nextLine();
    }

    /** Prints a numbered list of choices. */
    public static void listOptions(String[] options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("   %2d. %s%n", i + 1, options[i]);
        }
        System.out.println("    0. Back");
    }
}
