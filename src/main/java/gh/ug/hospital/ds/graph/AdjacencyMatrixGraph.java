package gh.ug.hospital.ds.graph;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22248029
 *
 * The hospital map stored as a big grid.
 *
 * Every place gets a row and a column. The number where row 3 meets column 7
 * is the cost of going from the third place to the seventh. If there is no
 * path, we store NO_PATH.
 *
 * Asking "is there a path from here to there" is one lookup, which is faster
 * than the list version. But the grid holds a number for every possible pair,
 * so 52 places means 2704 slots even though only 116 of them are real paths.
 * On a bigger map that waste becomes serious.
 *
 * The grid has a fixed size, so we must be told the largest number of places
 * up front. That is a real difference from the list version, and worth
 * saying out loud in the report.
 *
 * Speed:
 *   hasEdge, edgeWeight   O(1)
 *   neighbours            O(places), because we scan the whole row
 *   memory                O(places squared)
 */
public class AdjacencyMatrixGraph implements GraphView {

    /** Stored where there is no path. Infinity is convenient for Dijkstra. */
    public static final double NO_PATH = Double.POSITIVE_INFINITY;

    private final double[][] grid;
    private final String[] placeAt;                 // row number  -> place name
    private final HashBackedMap<String, Integer> rowOf;  // place name -> row number
    private int placeCount;
    private int edgeTotal;

    public AdjacencyMatrixGraph(int maximumPlaces) {
        if (maximumPlaces < 1) {
            maximumPlaces = 1;
        }
        this.grid = new double[maximumPlaces][maximumPlaces];
        this.placeAt = new String[maximumPlaces];
        this.rowOf = new HashBackedMap<>();
        this.placeCount = 0;
        this.edgeTotal = 0;

        // Start with no paths anywhere.
        for (int r = 0; r < maximumPlaces; r++) {
            for (int c = 0; c < maximumPlaces; c++) {
                grid[r][c] = NO_PATH;
            }
        }
    }

    @Override
    public void addVertex(String vertex) {
        if (vertex == null) {
            throw new StructureException("A place cannot be null.");
        }
        if (rowOf.containsKey(vertex)) {
            return;
        }
        if (placeCount >= grid.length) {
            throw new StructureException(
                    "This matrix was built for " + grid.length
                            + " places and they are all used.");
        }
        rowOf.put(vertex, placeCount);
        placeAt[placeCount] = vertex;
        placeCount++;
    }

    @Override
    public void addEdge(String from, String to, double weightMinutes) {
        int r = rowFor(from);
        int c = rowFor(to);

        if (grid[r][c] == NO_PATH) {
            edgeTotal++;
        }
        grid[r][c] = weightMinutes;
        grid[c][r] = weightMinutes;     // paths go both ways
    }

    @Override
    public boolean hasEdge(String from, String to) {
        return grid[rowFor(from)][rowFor(to)] != NO_PATH;
    }

    @Override
    public double edgeWeight(String from, String to) {
        double weight = grid[rowFor(from)][rowFor(to)];
        if (weight == NO_PATH) {
            throw new StructureException(
                    "There is no path from " + from + " to " + to + ".");
        }
        return weight;
    }

    @Override
    public DynamicArray<String> neighbours(String vertex) {
        int r = rowFor(vertex);
        DynamicArray<String> names = new DynamicArray<>();
        // We have to look at the whole row, even the empty parts.
        for (int c = 0; c < placeCount; c++) {
            if (grid[r][c] != NO_PATH) {
                names.add(placeAt[c]);
            }
        }
        return names;
    }

    @Override
    public int degree(String vertex) {
        return neighbours(vertex).size();
    }

    @Override
    public DynamicArray<String> vertices() {
        DynamicArray<String> names = new DynamicArray<>();
        for (int i = 0; i < placeCount; i++) {
            names.add(placeAt[i]);
        }
        return names;
    }

    @Override
    public int vertexCount() {
        return placeCount;
    }

    @Override
    public int edgeCount() {
        return edgeTotal;
    }

    @Override
    public boolean hasVertex(String vertex) {
        return rowOf.containsKey(vertex);
    }

    @Override
    public void clear() {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid.length; c++) {
                grid[r][c] = NO_PATH;
            }
            placeAt[r] = null;
        }
        rowOf.clear();
        placeCount = 0;
        edgeTotal = 0;
    }

    private int rowFor(String vertex) {
        Integer row = rowOf.get(vertex);
        if (row == null) {
            throw new StructureException(
                    "The graph does not know a place called " + vertex + ".");
        }
        return row.intValue();
    }

    /** How many slots the grid holds, real paths or not. */
    public int slotCount() {
        return grid.length * grid.length;
    }

    /** How many of those slots hold a real path. Shows the waste. */
    public double fillRate() {
        if (placeCount == 0) {
            return 0.0;
        }
        return (double) (edgeTotal * 2) / (double) (placeCount * placeCount);
    }

    @Override
    public String kind() {
        return "adjacency matrix";
    }

    @Override
    public String toTraceString() {
        StringBuilder out = new StringBuilder();
        out.append("adjacency matrix: ").append(placeCount).append(" places, ")
                .append(edgeTotal).append(" paths\n");
        out.append("grid holds ").append(placeCount * placeCount)
                .append(" slots, of which ").append(edgeTotal * 2)
                .append(" are real paths (")
                .append(String.format("%.1f", fillRate() * 100)).append("% full)\n");

        // Show a small top-left corner of the grid. Printing 52 by 52 is useless.
        int show = placeCount;
        if (show > 6) {
            show = 6;
        }

        out.append("        ");
        for (int c = 0; c < show; c++) {
            out.append(String.format("%8s", placeAt[c]));
        }
        out.append("\n");

        for (int r = 0; r < show; r++) {
            out.append(String.format("%8s", placeAt[r]));
            for (int c = 0; c < show; c++) {
                if (grid[r][c] == NO_PATH) {
                    out.append(String.format("%8s", "."));
                } else {
                    out.append(String.format("%8.2f", grid[r][c]));
                }
            }
            out.append("\n");
        }
        if (placeCount > show) {
            out.append("(showing the first ").append(show)
                    .append(" of ").append(placeCount).append(" places)\n");
        }
        return out.toString();
    }
}
