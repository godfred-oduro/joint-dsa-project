package gh.ug.hospital.ds.graph;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 22248029
 *
 * The hospital map stored as a list of neighbours for each place.
 *
 * For every place we keep a small list saying "from here you can reach
 * L016 in 0.12 minutes, and L018 in 0.34 minutes". Nothing is stored about
 * pairs of places that are not joined.
 *
 * That makes this the right choice for our map, because the hospital has 52
 * places and only 116 paths. A place is joined to four or five others, not
 * to fifty. Walking a place's neighbours is therefore quick.
 *
 * Compare with AdjacencyMatrixGraph, which stores all 52 x 52 pairs whether
 * they exist or not.
 *
 * Speed:
 *   addEdge                  O(1)
 *   hasEdge, edgeWeight      O(d), where d is how many paths touch that place
 *   neighbours               O(d)
 *   memory                   O(places + paths)
 */
public class AdjacencyListGraph implements GraphView {

    /** One entry in a place's neighbour list. */
    private static class Link {
        final String to;
        final double weight;

        Link(String to, double weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    private final HashBackedMap<String, DynamicArray<Link>> lists;
    private final DynamicArray<String> allVertices;
    private int edgeTotal;

    public AdjacencyListGraph() {
        lists = new HashBackedMap<>();
        allVertices = new DynamicArray<>();
        edgeTotal = 0;
    }

    @Override
    public void addVertex(String vertex) {
        if (vertex == null) {
            throw new StructureException("A place cannot be null.");
        }
        if (lists.containsKey(vertex)) {
            return;
        }
        lists.put(vertex, new DynamicArray<Link>());
        allVertices.add(vertex);
    }

    @Override
    public void addEdge(String from, String to, double weightMinutes) {
        requireVertex(from);
        requireVertex(to);

        if (hasEdge(from, to)) {
            return;     // already joined, do not count it twice
        }

        lists.get(from).add(new Link(to, weightMinutes));
        lists.get(to).add(new Link(from, weightMinutes));   // paths go both ways
        edgeTotal++;
    }

    @Override
    public boolean hasEdge(String from, String to) {
        requireVertex(from);
        requireVertex(to);

        DynamicArray<Link> links = lists.get(from);
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).to.equals(to)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public double edgeWeight(String from, String to) {
        requireVertex(from);
        requireVertex(to);

        DynamicArray<Link> links = lists.get(from);
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).to.equals(to)) {
                return links.get(i).weight;
            }
        }
        throw new StructureException(
                "There is no path from " + from + " to " + to + ".");
    }

    @Override
    public DynamicArray<String> neighbours(String vertex) {
        requireVertex(vertex);
        DynamicArray<Link> links = lists.get(vertex);
        DynamicArray<String> names = new DynamicArray<>();
        for (int i = 0; i < links.size(); i++) {
            names.add(links.get(i).to);
        }
        return names;
    }

    @Override
    public int degree(String vertex) {
        requireVertex(vertex);
        return lists.get(vertex).size();
    }

    @Override
    public DynamicArray<String> vertices() {
        DynamicArray<String> copy = new DynamicArray<>();
        for (int i = 0; i < allVertices.size(); i++) {
            copy.add(allVertices.get(i));
        }
        return copy;
    }

    @Override
    public int vertexCount() {
        return allVertices.size();
    }

    @Override
    public int edgeCount() {
        return edgeTotal;
    }

    @Override
    public boolean hasVertex(String vertex) {
        return lists.containsKey(vertex);
    }

    @Override
    public void clear() {
        lists.clear();
        allVertices.clear();
        edgeTotal = 0;
    }

    private void requireVertex(String vertex) {
        if (!lists.containsKey(vertex)) {
            throw new StructureException(
                    "The graph does not know a place called " + vertex + ".");
        }
    }

    @Override
    public String kind() {
        return "adjacency list";
    }

    @Override
    public String toTraceString() {
        StringBuilder out = new StringBuilder();
        out.append("adjacency list: ").append(vertexCount()).append(" places, ")
                .append(edgeTotal).append(" paths\n");

        // Show the first eight places, otherwise 52 lines fill the screen.
        int show = allVertices.size();
        if (show > 8) {
            show = 8;
        }
        for (int i = 0; i < show; i++) {
            String place = allVertices.get(i);
            out.append("  ").append(place).append(" -> ");
            DynamicArray<Link> links = lists.get(place);
            for (int j = 0; j < links.size(); j++) {
                if (j > 0) {
                    out.append(", ");
                }
                out.append(links.get(j).to).append("(")
                        .append(String.format("%.2f", links.get(j).weight)).append(")");
            }
            out.append("\n");
        }
        if (allVertices.size() > show) {
            out.append("  ... and ").append(allVertices.size() - show)
                    .append(" more places\n");
        }
        return out.toString();
    }
}
