package gh.ug.hospital.db;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.exception.DomainException;
import gh.ug.hospital.model.Location;
import gh.ug.hospital.model.Resource;
import gh.ug.hospital.model.Road;
import gh.ug.hospital.model.ServiceRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Owner: 22015166
 *
 * Reads the four seed CSV files and puts them into the database.
 *
 * This runs once, from menu option M1. After that the program always reads
 * from the database, never from the files. The brief is clear about that:
 * the data has to live in a real database, not be loaded from CSV every time
 * the program starts.
 *
 * The reading is done by hand rather than with a CSV library, because our
 * files contain commas inside the notes column and we need to handle the
 * quotation marks around those ourselves. It also keeps the project free of
 * another dependency.
 */
public class SeedLoader {

    public static final String FOLDER = "sql/seed/";

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Loads all four files in the right order and reports what happened. */
    public String loadEverything() throws SQLException, IOException {
        StringBuilder report = new StringBuilder();

        // Order matters. Roads point at locations, and requests point at both
        // locations and resources, so locations have to go in first.
        DynamicArray<Location> locations = readLocations();
        report.append(new LocationDao().saveAll(locations))
                .append(" locations loaded\n");

        DynamicArray<Road> roads = readRoads();
        report.append(new RoadDao().saveAll(roads))
                .append(" roads loaded\n");

        DynamicArray<Resource> resources = readResources();
        report.append(new ResourceDao().saveAll(resources))
                .append(" resources loaded\n");

        DynamicArray<ServiceRequest> requests = readRequests();
        report.append(new ServiceRequestDao().saveAll(requests))
                .append(" service requests loaded\n");

        return report.toString();
    }

    // ---------------- reading each file ----------------

    public DynamicArray<Location> readLocations() throws IOException {
        DynamicArray<Location> out = new DynamicArray<>();

        DynamicArray<String[]> rows = readCsv(FOLDER + "locations.csv");
        for (int i = 0; i < rows.size(); i++) {
            String[] cell = rows.get(i);
            out.add(new Location(
                    cell[0], cell[1], cell[2], cell[3],
                    Double.parseDouble(cell[4]),
                    Double.parseDouble(cell[5]),
                    cell[6], cell[7], cell[8], cell[9],
                    valueOrEmpty(cell, 10)));
        }
        return out;
    }

    public DynamicArray<Road> readRoads() throws IOException {
        DynamicArray<Road> out = new DynamicArray<>();

        DynamicArray<String[]> rows = readCsv(FOLDER + "roads.csv");
        for (int i = 0; i < rows.size(); i++) {
            String[] cell = rows.get(i);

            Road road = new Road(
                    cell[0], cell[1], cell[2],
                    Double.parseDouble(cell[3]),
                    Double.parseDouble(cell[4]),
                    Double.parseDouble(cell[5]),
                    cell[6], cell[7], cell[8],
                    Double.parseDouble(cell[9]),
                    Double.parseDouble(cell[10]),
                    Double.parseDouble(cell[11]),
                    cell[12], cell[13],
                    cell[16], cell[17], valueOrEmpty(cell, 18));

            // Columns 14 and 15 are the two stopwatch readings. They are
            // empty until someone walks the paths on site.
            road.setObservedTime0900(decimalOrNull(cell, 14));
            road.setObservedTime1500(decimalOrNull(cell, 15));

            out.add(road);
        }
        return out;
    }

    public DynamicArray<Resource> readResources() throws IOException {
        DynamicArray<Resource> out = new DynamicArray<>();

        DynamicArray<String[]> rows = readCsv(FOLDER + "resources.csv");
        for (int i = 0; i < rows.size(); i++) {
            String[] cell = rows.get(i);
            out.add(new Resource(
                    cell[0], cell[1], cell[2],
                    Integer.parseInt(cell[3]),
                    cell[4], cell[5],
                    timeOrNull(cell, 6),
                    timeOrNull(cell, 7),
                    cell[8], cell[9]));
        }
        return out;
    }

    public DynamicArray<ServiceRequest> readRequests() throws IOException {
        DynamicArray<ServiceRequest> out = new DynamicArray<>();

        DynamicArray<String[]> rows = readCsv(FOLDER + "service_requests.csv");
        for (int i = 0; i < rows.size(); i++) {
            String[] cell = rows.get(i);

            Integer tews = null;
            if (cell[9] != null && !cell[9].isEmpty()) {
                tews = Integer.valueOf(Integer.parseInt(cell[9]));
            }

            out.add(new ServiceRequest(
                    cell[0], cell[1], emptyToNull(cell[2]), cell[3],
                    Integer.parseInt(cell[4]),
                    LocalDateTime.parse(cell[5], TIMESTAMP),
                    LocalDateTime.parse(cell[6], TIMESTAMP),
                    cell[7], cell[8], tews, cell[10],
                    "1".equals(cell[11]),
                    "1".equals(cell[12]),
                    valueOrEmpty(cell, 14),
                    Integer.parseInt(cell[15])));
        }
        return out;
    }

    // ---------------- the CSV reading itself ----------------

    /**
     * Reads a CSV file, skipping the header line.
     *
     * Splitting on every comma would break, because the notes column has
     * commas inside it and is wrapped in quotation marks. So we walk the line
     * one character at a time and only treat a comma as a separator when we
     * are not inside quotation marks.
     */
    public DynamicArray<String[]> readCsv(String path) throws IOException {
        DynamicArray<String[]> rows = new DynamicArray<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));

            String line = reader.readLine();     // throw away the header
            if (line == null) {
                throw new DomainException(path + " is empty.");
            }

            line = reader.readLine();
            while (line != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(splitLine(line));
                }
                line = reader.readLine();
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return rows;
    }

    /** Splits one line into its cells, respecting quotation marks. */
    private String[] splitLine(String line) {
        DynamicArray<String> cells = new DynamicArray<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char letter = line.charAt(i);

            if (letter == '"') {
                insideQuotes = !insideQuotes;
            } else if (letter == ',' && !insideQuotes) {
                cells.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(letter);
            }
        }
        cells.add(current.toString().trim());

        String[] asArray = new String[cells.size()];
        for (int i = 0; i < cells.size(); i++) {
            asArray[i] = cells.get(i);
        }
        return asArray;
    }

    // ---------------- small helpers ----------------

    private String valueOrEmpty(String[] cells, int position) {
        if (position >= cells.length) {
            return "";
        }
        return cells[position];
    }

    private String emptyToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    private Double decimalOrNull(String[] cells, int position) {
        if (position >= cells.length || cells[position].isEmpty()) {
            return null;
        }
        return Double.valueOf(Double.parseDouble(cells[position]));
    }

    private LocalTime timeOrNull(String[] cells, int position) {
        if (position >= cells.length || cells[position].isEmpty()) {
            return null;
        }
        return LocalTime.parse(cells[position]);
    }
}
