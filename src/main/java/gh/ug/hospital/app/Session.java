package gh.ug.hospital.app;

import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.graph.AdjacencyListGraph;
import gh.ug.hospital.ds.graph.AdjacencyMatrixGraph;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.model.Location;
import gh.ug.hospital.model.Resource;
import gh.ug.hospital.model.Road;
import gh.ug.hospital.model.ServiceRequest;
import gh.ug.hospital.ops.UndoService;

/**
 * Owner: 22248029
 *
 * Holds everything the program has loaded, so the menus can share it.
 *
 * Without this, every menu would have to go back to the database each time,
 * and the graph would be rebuilt over and over. Loading once and keeping it
 * here is both faster and simpler to follow.
 *
 * The map is built twice on purpose, once as a list of neighbours and once
 * as a grid. The brief asks for both, and menu option M2.17 prints them side
 * by side so the difference is visible.
 */
public class Session {

    private DynamicArray<Location> locations;
    private DynamicArray<Road> roads;
    private DynamicArray<Resource> resources;
    private DynamicArray<ServiceRequest> requests;

    private AdjacencyListGraph listMap;
    private AdjacencyMatrixGraph matrixMap;

    private final UndoService undoService;

    private boolean loaded;

    public Session() {
        this.locations = new DynamicArray<>();
        this.roads = new DynamicArray<>();
        this.resources = new DynamicArray<>();
        this.requests = new DynamicArray<>();
        this.undoService = new UndoService();
        this.loaded = false;
    }

    public void setData(DynamicArray<Location> locations, DynamicArray<Road> roads,
                        DynamicArray<Resource> resources,
                        DynamicArray<ServiceRequest> requests) {
        this.locations = locations;
        this.roads = roads;
        this.resources = resources;
        this.requests = requests;
        buildMaps();
        this.loaded = true;
    }

    /**
     * Builds both versions of the map from the places and paths.
     *
     * The matrix needs to know its largest size up front, because it is a
     * fixed grid. The list does not. That difference is worth mentioning in
     * the report, because it is a real practical cost of the matrix.
     */
    private void buildMaps() {
        listMap = new AdjacencyListGraph();
        matrixMap = new AdjacencyMatrixGraph(Math.max(locations.size(), 1));

        for (int i = 0; i < locations.size(); i++) {
            String id = locations.get(i).getLocationId();
            listMap.addVertex(id);
            matrixMap.addVertex(id);
        }

        for (int i = 0; i < roads.size(); i++) {
            Road road = roads.get(i);
            listMap.addEdge(road.getFromLocationId(), road.getToLocationId(),
                    road.getWeightedCostMinutes());
            matrixMap.addEdge(road.getFromLocationId(), road.getToLocationId(),
                    road.getWeightedCostMinutes());
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Complains politely if a menu is used before the data is loaded.
     *
     * @return true if the data is there, false if the user needs to load first
     */
    public boolean requireLoaded() {
        if (loaded) {
            return true;
        }
        Console.problem("No data is loaded yet. Choose option 1 from the main menu "
                + "and load the database first.");
        return false;
    }

    public DynamicArray<Location> getLocations() {
        return locations;
    }

    public DynamicArray<Road> getRoads() {
        return roads;
    }

    public DynamicArray<Resource> getResources() {
        return resources;
    }

    public DynamicArray<ServiceRequest> getRequests() {
        return requests;
    }

    /** The map stored as a list of neighbours. This is the everyday one. */
    public GraphView getListMap() {
        return listMap;
    }

    /** The map stored as a grid, for the side by side comparison. */
    public GraphView getMatrixMap() {
        return matrixMap;
    }

    public UndoService getUndoService() {
        return undoService;
    }

    /** Finds a place by its code, or null. */
    public Location findLocation(String locationId) {
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).getLocationId().equals(locationId)) {
                return locations.get(i);
            }
        }
        return null;
    }

    /** Finds a request by its code, or null. */
    public ServiceRequest findRequest(String requestId) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getRequestId().equals(requestId)) {
                return requests.get(i);
            }
        }
        return null;
    }

    public String summary() {
        if (!loaded) {
            return "Nothing loaded yet.";
        }
        return "Loaded: " + locations.size() + " places, " + roads.size() + " paths, "
                + resources.size() + " resources, " + requests.size() + " requests.";
    }
}
