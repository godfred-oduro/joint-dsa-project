package gh.ug.hospital.ds;

import gh.ug.hospital.exception.StructureException;

/**
 * Owner: 11102882
 *
 * Keeps track of which things are joined together.
 *
 * Also called union-find. Every item starts in its own little group. You can
 * join two groups, and you can ask which group something belongs to. That is
 * all it does, and it does it very fast.
 *
 * Kruskal's algorithm uses it to answer one question over and over: "if I add
 * this road, does it create a loop?" If both ends are already in the same
 * group, then yes, so skip that road.
 *
 * Two tricks make it fast:
 *
 *   Union by rank. When joining two groups, hang the shorter tree under the
 *   taller one. That stops long chains forming.
 *
 *   Path compression. While walking up to find the group leader, point every
 *   node we pass straight at the leader. Next time the walk is one step.
 *
 * Together these make find() so close to O(1) that the difference does not
 * matter in practice.
 *
 * Because the items are location codes such as "L015", we store them in our
 * own map rather than assuming they are numbered 0 to n.
 */
public class DisjointSet<T> {

    private final HashBackedMap<T, T> parent;   // who is directly above me
    private final HashBackedMap<T, Integer> rank;  // rough height of my tree
    private int groupCount;

    /** Counts how many nodes path compression re-pointed. Used in the trace. */
    private long compressions;

    public DisjointSet() {
        parent = new HashBackedMap<>();
        rank = new HashBackedMap<>();
        groupCount = 0;
        compressions = 0;
    }

    /** Starts a new group holding only this item. */
    public void makeSet(T item) {
        if (parent.containsKey(item)) {
            return;     // already known, nothing to do
        }
        parent.put(item, item);     // at the start, everyone is their own leader
        rank.put(item, 0);
        groupCount++;
    }

    /**
     * Finds the leader of this item's group.
     *
     * On the way back down we point everything we passed straight at the
     * leader, which is the path compression trick.
     */
    public T find(T item) {
        if (!parent.containsKey(item)) {
            throw new StructureException(
                    item + " was never added with makeSet, so it has no group.");
        }
        T above = parent.get(item);
        if (above.equals(item)) {
            return item;            // this item is the leader
        }
        T leader = find(above);
        if (!above.equals(leader)) {
            compressions++;
        }
        parent.put(item, leader);   // point straight at the leader
        return leader;
    }

    /**
     * Joins the two groups. Hands back true if they were separate, false if
     * they were already together, which is what Kruskal checks.
     */
    public boolean union(T a, T b) {
        T leaderA = find(a);
        T leaderB = find(b);

        if (leaderA.equals(leaderB)) {
            return false;           // already joined, adding this would make a loop
        }

        int rankA = rank.get(leaderA);
        int rankB = rank.get(leaderB);

        // Hang the shorter tree under the taller one.
        if (rankA < rankB) {
            parent.put(leaderA, leaderB);
        } else if (rankA > rankB) {
            parent.put(leaderB, leaderA);
        } else {
            parent.put(leaderB, leaderA);
            rank.put(leaderA, rankA + 1);   // same height, so the winner grows by one
        }

        groupCount--;
        return true;
    }

    /** True if these two are in the same group. */
    public boolean connected(T a, T b) {
        return find(a).equals(find(b));
    }

    /** How many separate groups there are. */
    public int componentCount() {
        return groupCount;
    }

    public int size() {
        return parent.size();
    }

    public boolean isEmpty() {
        return parent.size() == 0;
    }

    public long getCompressions() {
        return compressions;
    }

    public void clear() {
        parent.clear();
        rank.clear();
        groupCount = 0;
        compressions = 0;
    }

    public String toTraceString() {
        return "items=" + parent.size()
                + "  groups=" + groupCount
                + "  path compressions=" + compressions;
    }
}
