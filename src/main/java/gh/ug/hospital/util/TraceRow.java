package gh.ug.hospital.util;

/**
 * Owner: 22399010
 *
 * One line of a trace table (guide Part 22).
 *
 * Every trace table in the report has the same six columns, so that six
 * different people do not produce six different layouts:
 *
 *   Step | Operation | State After | Comparisons | Swaps or Moves | Note
 *
 * Some algorithms need extra columns, for example binary search wants
 * Low, High and Middle. Those go in the "extras" array.
 */
public class TraceRow {

    private final int step;
    private final String operation;
    private final String stateAfter;
    private final long comparisons;
    private final long swaps;
    private final String note;
    private final String[] extras;

    /** Use this when the algorithm needs no extra columns. */
    public TraceRow(int step, String operation, String stateAfter,
                    long comparisons, long swaps, String note) {
        this(step, operation, stateAfter, comparisons, swaps, note, new String[0]);
    }

    public TraceRow(int step, String operation, String stateAfter,
                    long comparisons, long swaps, String note, String[] extras) {
        this.step = step;
        this.operation = operation;
        this.stateAfter = stateAfter;
        this.comparisons = comparisons;
        this.swaps = swaps;
        this.note = note;
        if (extras == null) {
            this.extras = new String[0];
        } else {
            this.extras = extras;
        }
    }

    public int getStep() {
        return step;
    }

    public String getOperation() {
        return operation;
    }

    public String getStateAfter() {
        return stateAfter;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getSwaps() {
        return swaps;
    }

    public String getNote() {
        return note;
    }

    public String[] getExtras() {
        return extras;
    }

    /**
     * Turns this row into the cells of a table, ready for TablePrinter.
     * The extra columns are added on the end.
     */
    public String[] toCells() {
        String[] cells = new String[6 + extras.length];
        cells[0] = String.valueOf(step);
        cells[1] = operation;
        cells[2] = stateAfter;
        cells[3] = String.valueOf(comparisons);
        cells[4] = String.valueOf(swaps);
        cells[5] = note;
        for (int i = 0; i < extras.length; i++) {
            cells[6 + i] = extras[i];
        }
        return cells;
    }
}
