package gh.ug.hospital.util;

/**
 * Owner: 22224236
 *
 * What every algorithm hands back (guide Part 28.4).
 *
 * It holds three things:
 *   1. the answer, whatever that algorithm produces
 *   2. how much work it did, so the timing experiments and the primitive
 *      operation counts both come from the same run
 *   3. the trace rows, so the evidence menu can print a trace table
 *
 * Note on the trace rows: we grow our own array instead of using an
 * ArrayList, because constraint C19 bans Java's own collections from the
 * marked code. This class is deliberately self contained so that it can be
 * written in Week 1, before anyone has built DynamicArray.
 */
public class AlgorithmResult {

    private Object answer;
    private long comparisons;
    private long swaps;
    private long elapsedNanos;

    private TraceRow[] rows;
    private int rowCount;

    public AlgorithmResult() {
        this.rows = new TraceRow[16];
        this.rowCount = 0;
        this.comparisons = 0;
        this.swaps = 0;
        this.elapsedNanos = 0;
    }

    // ---- the answer ----

    public Object getAnswer() {
        return answer;
    }

    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    // ---- counting the work ----

    public long getComparisons() {
        return comparisons;
    }

    /** Call this every time the algorithm compares two things. */
    public void countComparison() {
        comparisons++;
    }

    public void addComparisons(long howMany) {
        comparisons += howMany;
    }

    public long getSwaps() {
        return swaps;
    }

    /** Call this every time the algorithm swaps or moves something. */
    public void countSwap() {
        swaps++;
    }

    public void addSwaps(long howMany) {
        swaps += howMany;
    }

    /** Comparisons plus swaps. This is the "primitive operations" figure. */
    public long getPrimitiveOperations() {
        return comparisons + swaps;
    }

    // ---- how long it took ----

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public void setElapsedNanos(long elapsedNanos) {
        this.elapsedNanos = elapsedNanos;
    }

    /** Time in milliseconds, for printing. Nanoseconds are hard to read. */
    public double getElapsedMillis() {
        return elapsedNanos / 1000000.0;
    }

    // ---- the trace rows ----

    /**
     * Adds one line to the trace. The step number is filled in for you,
     * starting at 1, so nobody has to remember to count.
     */
    public void addTrace(String operation, String stateAfter, String note) {
        addTrace(operation, stateAfter, note, new String[0]);
    }

    /** Same as above, but with the extra columns some algorithms need. */
    public void addTrace(String operation, String stateAfter, String note, String[] extras) {
        growIfFull();
        rows[rowCount] = new TraceRow(rowCount + 1, operation, stateAfter,
                comparisons, swaps, note, extras);
        rowCount++;
    }

    private void growIfFull() {
        if (rowCount < rows.length) {
            return;
        }
        // Double the array and copy everything across. Same idea as DynamicArray.
        TraceRow[] bigger = new TraceRow[rows.length * 2];
        for (int i = 0; i < rows.length; i++) {
            bigger[i] = rows[i];
        }
        rows = bigger;
    }

    public int getTraceCount() {
        return rowCount;
    }

    public TraceRow getTraceRow(int index) {
        if (index < 0 || index >= rowCount) {
            throw new IndexOutOfBoundsException(
                    "Trace row " + index + " does not exist. There are " + rowCount + " rows.");
        }
        return rows[index];
    }

    /** All the trace rows, trimmed to the right length. */
    public TraceRow[] getTraceRows() {
        TraceRow[] exact = new TraceRow[rowCount];
        for (int i = 0; i < rowCount; i++) {
            exact[i] = rows[i];
        }
        return exact;
    }

    /** One line summary, useful after running an algorithm from the menu. */
    public String summary() {
        return "comparisons=" + comparisons
                + "  swaps=" + swaps
                + "  time=" + String.format("%.3f", getElapsedMillis()) + " ms"
                + "  trace rows=" + rowCount;
    }
}
