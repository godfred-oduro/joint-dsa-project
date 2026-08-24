package gh.ug.hospital.app;

import gh.ug.hospital.db.AlgorithmRunDao;
import gh.ug.hospital.db.AuditEventDao;
import gh.ug.hospital.db.ConnectionManager;
import gh.ug.hospital.db.LocationDao;
import gh.ug.hospital.db.ResourceDao;
import gh.ug.hospital.db.RoadDao;
import gh.ug.hospital.db.SeedLoader;
import gh.ug.hospital.db.ServiceRequestDao;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.model.Location;
import gh.ug.hospital.model.Resource;
import gh.ug.hospital.model.Road;
import gh.ug.hospital.model.ServiceRequest;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Owner: 22248029
 *
 * Menu option 1. Connects to the database, loads everything, and checks it.
 *
 * The checking matters more than it looks. If a coordinate has a digit typed
 * wrongly, the routing will still produce an answer, just a silly one. Far
 * better to catch it here and say which row is wrong.
 */
public class DatabaseMenu {

    private final Session session;

    private final LocationDao locationDao = new LocationDao();
    private final RoadDao roadDao = new RoadDao();
    private final ResourceDao resourceDao = new ResourceDao();
    private final ServiceRequestDao requestDao = new ServiceRequestDao();
    private final AlgorithmRunDao runDao = new AlgorithmRunDao();
    private final AuditEventDao auditDao = new AuditEventDao();

    private static final String[] OPTIONS = {
            "Test the connection",
            "Put the seed files INTO the database (do this once, first)",
            "Load everything from the database",
            "Show how many rows each table holds",
            "Show how much data is still estimated",
            "Show the mix of urgency colours"
    };

    public DatabaseMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            Console.heading("1. DATABASE");
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Choose", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }

            try {
                handle(choice);
            } catch (SQLException problem) {
                Console.problem("The database said: " + problem.getMessage());
                Console.say("  Check that MySQL is running and that "
                        + "config.properties has the right details.");
            }
            Console.pause();
        }
    }

    private void handle(int choice) throws SQLException {
        if (choice == 1) {
            Console.say(ConnectionManager.testConnection());
        } else if (choice == 2) {
            seedTheDatabase();
        } else if (choice == 3) {
            loadEverything();
        } else if (choice == 4) {
            showCounts();
        } else if (choice == 5) {
            showEstimated();
        } else if (choice == 6) {
            Console.say(requestDao.countByColour());
            Console.say("The guide expects RED 12, ORANGE 33, YELLOW 120, "
                    + "GREEN 132, BLUE 3.");
        }
    }

    /**
     * Reads the four CSV files in sql/seed and puts them into the database.
     *
     * This only needs doing once, the first time the program is run on a
     * machine. After that the data lives in MySQL and the program reads it
     * from there, which is what the brief asks for.
     *
     * Running it again is harmless. Every insert updates the row if the code
     * is already there rather than making a second copy.
     */
    private void seedTheDatabase() throws SQLException {
        Console.say("Reading the CSV files in sql/seed and writing them to MySQL.");
        Console.say("This only needs doing once.");
        Console.blank();

        try {
            SeedLoader loader = new SeedLoader();
            Console.say(loader.loadEverything());
            Console.blank();
            Console.say("Now choose option 3 to load it into the program.");

        } catch (IOException problem) {
            Console.problem("Could not read the seed files: " + problem.getMessage());
            Console.say("  Start the program from the project folder, the one that");
            Console.say("  holds pom.xml, so that sql/seed can be found.");
        }
    }

    private void loadEverything() throws SQLException {
        Console.say("Reading the four tables...");

        DynamicArray<Location> locations = locationDao.findAll();
        DynamicArray<Road> roads = roadDao.findAll();
        DynamicArray<Resource> resources = resourceDao.findAll();
        DynamicArray<ServiceRequest> requests = requestDao.findAll();

        Console.say("  places:    " + locations.size());
        Console.say("  paths:     " + roads.size());
        Console.say("  resources: " + resources.size());
        Console.say("  requests:  " + requests.size());

        if (locations.size() == 0) {
            Console.problem("Nothing came back. Has the seed data been loaded? "
                    + "Run the SQL in sql/schema.sql first, then load the seed files.");
            return;
        }

        session.setData(locations, roads, resources, requests);

        Console.blank();
        Console.say("Building the map both ways...");
        Console.say("  " + session.getListMap().toTraceString());
        Console.say("Ready.");
    }

    private void showCounts() throws SQLException {
        Console.say("locations:       " + locationDao.count() + "   (the brief asks for 50 or more)");
        Console.say("roads:           " + roadDao.count() + "   (100 or more)");
        Console.say("resources:       " + resourceDao.count() + "   (30 or more)");
        Console.say("service_requests:" + requestDao.count() + "   (300 or more)");
        Console.say("algorithm_runs:  " + runDao.count() + "   (30 or more)");
        Console.say("audit_events:    " + auditDao.count() + "   (filled while running)");

        Console.blank();
        if (runDao.count() > 0) {
            if (runDao.allRunsOnOneMachine()) {
                Console.say("Every timing row names the same computer. Constraint C21 holds.");
            } else {
                Console.problem("Timing rows name more than one computer. "
                        + "That breaks constraint C21 and the graphs cannot be trusted. "
                        + "Rerun every experiment on one machine.");
            }
        }
    }

    /**
     * Counts how many rows are still worked out rather than measured.
     *
     * Six coordinates were read from Google Earth. The rest of the positions
     * inside the grounds were estimated between them, and every resource
     * count was scaled from the hospital's published staff total. The source
     * and confidence columns say which is which, and this adds them up.
     */
    private void showEstimated() {
        if (!session.requireLoaded()) {
            return;
        }

        int measured = 0;
        for (int i = 0; i < session.getLocations().size(); i++) {
            if (session.getLocations().get(i).isMeasured()) {
                measured++;
            }
        }
        int estimated = session.getLocations().size() - measured;

        int timed = 0;
        for (int i = 0; i < session.getRoads().size(); i++) {
            if (session.getRoads().get(i).hasBeenMeasured()) {
                timed++;
            }
        }

        Console.say("Places: " + measured + " measured, " + estimated + " estimated.");
        Console.say("Paths:  " + timed + " timed on site, "
                + (session.getRoads().size() - timed) + " still calculated.");
        Console.blank();
        Console.say("Everything still marked estimated is listed in the project");
        Console.say("guide, Part 29, with the person responsible for replacing it.");
    }
}
