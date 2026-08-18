package gh.ug.hospital.ds.graph;

import gh.ug.hospital.ds.DynamicArray;

/**
 * Owner: 22248029
 *
 * The list of things any graph in this project must be able to do.
 *
 * The project brief asks for the hospital map stored two different ways: as
 * an adjacency list and as an adjacency matrix. Rather than writing one
 * class that tries to be both, we write this short agreement and then two
 * classes that each keep it in their own way.
 *
 * That means menu option M2.17 can load the same 52 locations into both and
 * print them side by side, and experiment E6 can time the same algorithm
 * against each one. Neither would be easy with a single class.
 *
 * In our model a vertex is a place, such as "L015", and an edge is a path or
 * small road between two places, with a cost in minutes.
 */
public interface GraphView {

    /** Adds a place. Doing it twice does nothing the second time. */
    void addVertex(String vertex);

    /** Adds a path between two places. Paths work in both directions. */
    void addEdge(String from, String to, double weightMinutes);

    /** True if these two places are joined directly. */
    boolean hasEdge(String from, String to);

    /** The cost of going straight from one place to the other, in minutes. */
    double edgeWeight(String from, String to);

    /** Every place you can reach in one step from here. */
    DynamicArray<String> neighbours(String vertex);

    /** How many paths touch this place. */
    int degree(String vertex);

    /** Every place in the graph. */
    DynamicArray<String> vertices();

    int vertexCount();

    int edgeCount();

    boolean hasVertex(String vertex);

    void clear();

    /** A short description for the trace tables. */
    String toTraceString();

    /** Says which of the two kinds this is, for printing side by side. */
    String kind();
}
