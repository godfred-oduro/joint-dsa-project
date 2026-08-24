package gh.ug.hospital.app;

/**
 * Owner: 22248029
 *
 * The main menu. Shows the ten choices and hands control to whichever
 * smaller menu the user picks.
 *
 * The brief says an examiner must be able to reach every demonstration,
 * every algorithm, every test result and every timing run without opening a
 * single source file. This class is what makes that true.
 *
 * Everything the program has loaded lives in one Session object, which is
 * passed to each menu. That way the database is read once and the whole
 * program works on the same data.
 */
public class MenuRouter {

    private final Session session;

    private final DatabaseMenu databaseMenu;
    private final StructureMenu structureMenu;
    private final AlgorithmMenu algorithmMenu;
    private final OperationsMenu operationsMenu;
    private final BenchmarkMenu benchmarkMenu;
    private final EvidenceMenu evidenceMenu;
    private final AuditMenu auditMenu;
    private final ParameterMenu parameterMenu;

    public MenuRouter() {
        this.session = new Session();

        this.databaseMenu = new DatabaseMenu(session);
        this.structureMenu = new StructureMenu(session);
        this.algorithmMenu = new AlgorithmMenu(session);
        this.operationsMenu = new OperationsMenu(session);
        this.benchmarkMenu = new BenchmarkMenu(session);
        this.evidenceMenu = new EvidenceMenu(session);
        this.auditMenu = new AuditMenu(session);
        this.parameterMenu = new ParameterMenu(session);
    }

    /** Keeps showing the menu until the user chooses 0. */
    public void run() {
        boolean keepGoing = true;

        while (keepGoing) {
            showChoices();

            int choice = Console.askNumber("Choose", 0, 8);

            if (choice == 0) {
                keepGoing = false;
            } else if (choice == 1) {
                databaseMenu.show();
            } else if (choice == 2) {
                structureMenu.show();
            } else if (choice == 3) {
                algorithmMenu.show();
            } else if (choice == 4) {
                operationsMenu.show();
            } else if (choice == 5) {
                benchmarkMenu.show();
            } else if (choice == 6) {
                evidenceMenu.show();
            } else if (choice == 7) {
                auditMenu.show();
            } else if (choice == 8) {
                parameterMenu.show();
            }
        }
    }

    private void showChoices() {
        Console.heading("MAIN MENU");
        Console.say("  1  Database          connect, load, check, row counts");
        Console.say("  2  Data structures   demonstrate each of the 17");
        Console.say("  3  Algorithms        run each of the 17");
        Console.say("  4  Hospital work     triage, dispatch, routing, referrals, budget");
        Console.say("  5  Timing            run the six experiments, save the results");
        Console.say("  6  Evidence          trace tables, collisions, counterexamples");
        Console.say("  7  Audit and undo    what happened, and take it back");
        Console.say("  8  Settings          view and change P1 to P5 while running");
        Console.say("  0  Exit");
        Console.blank();
        Console.say(session.summary());
    }
}
