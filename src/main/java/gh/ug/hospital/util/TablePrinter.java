package gh.ug.hospital.util;

/**
 * Owner: 22367946
 *
 * Prints tables with the columns lined up (guide Part 11.5).
 *
 * The console has no table support, so we work out how wide each column
 * needs to be, then pad every cell with spaces to that width.
 */
public class TablePrinter {

    /** How many characters we allow in one cell before cutting it short. */
    private static final int MAX_CELL_WIDTH = 40;

    /**
     * Builds a table as one big string.
     *
     * @param headers the column titles
     * @param rows    the data, one array per row
     */
    public static String build(String[] headers, String[][] rows) {
        if (headers == null || headers.length == 0) {
            return "(no columns to show)";
        }

        int columnCount = headers.length;
        int[] widths = new int[columnCount];

        // Start each column as wide as its title.
        for (int c = 0; c < columnCount; c++) {
            widths[c] = safe(headers[c]).length();
        }

        // Then widen it if any cell in that column is longer.
        for (int r = 0; r < rows.length; r++) {
            for (int c = 0; c < columnCount && c < rows[r].length; c++) {
                int length = shorten(safe(rows[r][c])).length();
                if (length > widths[c]) {
                    widths[c] = length;
                }
            }
        }

        StringBuilder out = new StringBuilder();

        appendRow(out, headers, widths);
        appendDivider(out, widths);

        for (int r = 0; r < rows.length; r++) {
            appendRow(out, rows[r], widths);
        }

        return out.toString();
    }

    /** Prints the table straight to the console. */
    public static void print(String[] headers, String[][] rows) {
        System.out.println(build(headers, rows));
    }

    /** Prints a trace table, adding the six standard column titles. */
    public static void printTrace(AlgorithmResult result, String[] extraHeaders) {
        TraceRow[] traceRows = result.getTraceRows();

        int extraCount = 0;
        if (extraHeaders != null) {
            extraCount = extraHeaders.length;
        }

        String[] headers = new String[6 + extraCount];
        headers[0] = "Step";
        headers[1] = "Operation";
        headers[2] = "State After";
        headers[3] = "Comparisons";
        headers[4] = "Swaps/Moves";
        headers[5] = "Note";
        for (int i = 0; i < extraCount; i++) {
            headers[6 + i] = extraHeaders[i];
        }

        // Part 22.4 says keep traces to 25 rows and say so when we cut them.
        int limit = traceRows.length;
        boolean cut = false;
        if (limit > 25) {
            limit = 25;
            cut = true;
        }

        String[][] cells = new String[limit][];
        for (int i = 0; i < limit; i++) {
            cells[i] = traceRows[i].toCells();
        }

        print(headers, cells);

        if (cut) {
            System.out.println("(shortened at step 25 of " + traceRows.length + ")");
        }
    }

    // ---- small helpers ----

    private static void appendRow(StringBuilder out, String[] cells, int[] widths) {
        for (int c = 0; c < widths.length; c++) {
            String text = "";
            if (c < cells.length) {
                text = shorten(safe(cells[c]));
            }
            out.append(pad(text, widths[c]));
            if (c < widths.length - 1) {
                out.append("  ");
            }
        }
        out.append("\n");
    }

    private static void appendDivider(StringBuilder out, int[] widths) {
        for (int c = 0; c < widths.length; c++) {
            for (int i = 0; i < widths[c]; i++) {
                out.append('-');
            }
            if (c < widths.length - 1) {
                out.append("  ");
            }
        }
        out.append("\n");
    }

    private static String pad(String text, int width) {
        StringBuilder padded = new StringBuilder(text);
        while (padded.length() < width) {
            padded.append(' ');
        }
        return padded.toString();
    }

    private static String safe(String text) {
        if (text == null) {
            return "";
        }
        return text;
    }

    private static String shorten(String text) {
        if (text.length() <= MAX_CELL_WIDTH) {
            return text;
        }
        return text.substring(0, MAX_CELL_WIDTH - 3) + "...";
    }
}
