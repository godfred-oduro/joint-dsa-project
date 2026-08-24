package gh.ug.hospital.app;

/**
 * Owner: 22248029
 *
 * Where the program starts. This is the only main method in the project.
 *
 * It does almost nothing itself. All it does is print a banner and hand
 * control to MenuRouter, which shows the menu and keeps showing it until
 * somebody chooses to exit.
 *
 * Keeping this file tiny is deliberate. Seventeen people are writing
 * separate files, and everything meets here. If this file grew, everyone
 * would end up editing it and we would spend the project resolving merge
 * conflicts instead of writing algorithms.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        MenuRouter router = new MenuRouter();
        router.run();

        System.out.println();
        System.out.println("Goodbye.");
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("=================================================================");
        System.out.println("  UNIVERSITY OF GHANA HOSPITAL, LEGON");
        System.out.println("  Operations model");
        System.out.println();
        System.out.println("  DCIT 204 and DCIT 308 joint semester project, 16 members");
        System.out.println("=================================================================");
        System.out.println();
        System.out.println("  This models the 140-bed University of Ghana Hospital, Legon.");
        System.out.println("  It does not model the University of Ghana Medical Centre,");
        System.out.println("  which is a separate and much larger hospital nearby.");
        System.out.println();
        System.out.println("  Start with option 1 to connect to the database.");
        System.out.println();
    }
}
