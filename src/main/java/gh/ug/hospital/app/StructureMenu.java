package gh.ug.hospital.app;

import gh.ug.hospital.ds.BTree;
import gh.ug.hospital.ds.BinaryHeap;
import gh.ug.hospital.ds.BinarySearchTree;
import gh.ug.hospital.ds.ChainedHashTable;
import gh.ug.hospital.ds.CircularQueue;
import gh.ug.hospital.ds.DisjointSet;
import gh.ug.hospital.ds.DoublyLinkedList;
import gh.ug.hospital.ds.DynamicArray;
import gh.ug.hospital.ds.HashBackedMap;
import gh.ug.hospital.ds.HashBackedSet;
import gh.ug.hospital.ds.LinkedDeque;
import gh.ug.hospital.ds.LinkedQueue;
import gh.ug.hospital.ds.LinkedStack;
import gh.ug.hospital.ds.RedBlackTree;
import gh.ug.hospital.ds.SinglyLinkedList;
import gh.ug.hospital.util.Parameters;

import java.util.Comparator;

/**
 * Owner: 22248029
 *
 * Menu option 2. Demonstrates each of the 16 data structures.
 *
 * The numbering here is fixed and matches the project guide, because trace
 * table captions and report figures refer to it. M2.4 is always the stack.
 *
 * Each demonstration does three things: uses the structure normally, prints
 * its state, and shows the piece of evidence that structure owes to the
 * report. The circular queue shows its markers wrapping round to zero. The
 * red-black tree shows the shape before and after each rotation. The hash
 * tables show their collision counts.
 */
public class StructureMenu {

    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    private static final String[] OPTIONS = {
            "Dynamic array            (22399601)",
            "Singly linked list       (22392509)",
            "Doubly linked list       (22372680)",
            "Stack                    (22372639)",
            "Queue                    (22015166)",
            "Circular queue           (22395395)",
            "Deque                    (22367946)",
            "Binary search tree       (22305896)",
            "Hash table, chaining     (22370298)",
            "Set                      (22399010)",
            "Heap                     (22224236)",
            "Red-black tree           (22040255)",
            "B-tree                   (22104879)",
            "Map                      (22160537)",
            "Disjoint set             (11102882)",
            "Graph, list and matrix   (22248029)"
    };

    private final Session session;

    public StructureMenu(Session session) {
        this.session = session;
    }

    public void show() {
        while (true) {
            Console.heading("2. DATA STRUCTURES");
            Console.listOptions(OPTIONS);
            Console.blank();

            int choice = Console.askNumber("Demonstrate which", 1, OPTIONS.length);
            if (choice == Console.GO_BACK) {
                return;
            }
            demonstrate(choice);
            Console.pause();
        }
    }

    private void demonstrate(int choice) {
        Console.heading("2." + choice + "  " + OPTIONS[choice - 1]);

        if (choice == 1) {
            showDynamicArray();
        } else if (choice == 2) {
            showSinglyLinkedList();
        } else if (choice == 3) {
            showDoublyLinkedList();
        } else if (choice == 4) {
            showStack();
        } else if (choice == 5) {
            showQueue();
        } else if (choice == 6) {
            showCircularQueue();
        } else if (choice == 7) {
            showDeque();
        } else if (choice == 8) {
            showBinarySearchTree();
        } else if (choice == 9) {
            showChainedHashTable();
        } else if (choice == 10) {
            showSet();
        } else if (choice == 11) {
            showHeap();
        } else if (choice == 12) {
            showRedBlackTree();
        } else if (choice == 13) {
            showBTree();
        } else if (choice == 14) {
            showMap();
        } else if (choice == 15) {
            showDisjointSet();
        } else if (choice == 16) {
            showGraphs();
        }
    }

    // ---- 2.1 dynamic array ----

    private void showDynamicArray() {
        Console.say("Watch the array grow. It starts with room for 8 and doubles");
        Console.say("whenever it fills up. Nobody using it has to think about that.");
        Console.blank();

        DynamicArray<String> wards = new DynamicArray<>(2);
        String[] names = {"Male General", "Female General", "Maternity", "Labour",
                "Children", "Paediatric", "Casualty", "Resus", "Isolation"};

        for (int i = 0; i < names.length; i++) {
            wards.add(names[i]);
            Console.say("after adding " + names[i] + ": size=" + wards.size()
                    + " room for " + wards.capacity()
                    + " grown " + wards.getGrowCount() + " times");
        }
        Console.blank();
        Console.say(wards.toTraceString());
    }

    // ---- 2.2 singly linked list ----

    private void showSinglyLinkedList() {
        Console.say("A chain of boxes, each pointing at the next. Walking it is the");
        Console.say("only way to reach the middle, which is why get() is slow here.");
        Console.blank();

        SinglyLinkedList<String> list = new SinglyLinkedList<>();
        list.addLast("OPD");
        list.addLast("Pharmacy");
        list.addLast("Laboratory");
        list.addFirst("Main Gate");

        Console.say(list.toTraceString());
        Console.blank();
        Console.say("Walking it with our own iterator:");

        SinglyLinkedList<String>.ListWalker walker = list.walker();
        int step = 1;
        while (walker.hasNext()) {
            Console.say("  step " + step + ": " + walker.next());
            step++;
        }
    }

    // ---- 2.3 doubly linked list ----

    private void showDoublyLinkedList() {
        Console.say("Each box points both ways, so the list can be walked from");
        Console.say("either end. That extra pointer is what makes removeLast fast.");
        Console.blank();

        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("Triage");
        list.addLast("Resus");
        list.addLast("Casualty Ward");
        list.addLast("Theatre");

        Console.say(list.toTraceString());
    }

    // ---- 2.4 stack ----

    private void showStack() {
        Console.say("Last in, first out. This is what the undo log uses.");
        Console.blank();

        LinkedStack<String> undo = new LinkedStack<>();
        undo.push("assigned Q001 to porter RS004");
        Console.say("push -> " + undo.toTraceString());
        undo.push("assigned Q002 to porter RS007");
        Console.say("push -> " + undo.toTraceString());
        undo.push("cancelled Q001");
        Console.say("push -> " + undo.toTraceString());

        Console.blank();
        Console.say("undoing takes the newest first:");
        while (!undo.isEmpty()) {
            Console.say("  pop: " + undo.pop());
        }

        Console.blank();
        Console.say("Popping an empty stack now:");
        try {
            undo.pop();
        } catch (Exception expected) {
            Console.say("  " + expected.getMessage());
        }
    }

    // ---- 2.5 queue ----

    private void showQueue() {
        Console.say("First in, first out. An ordinary line of people.");
        Console.blank();

        LinkedQueue<String> waiting = new LinkedQueue<>();
        waiting.enqueue("Q010");
        waiting.enqueue("Q011");
        waiting.enqueue("Q012");
        Console.say(waiting.toTraceString());

        Console.blank();
        Console.say("serving: " + waiting.dequeue());
        Console.say("serving: " + waiting.dequeue());
        Console.say(waiting.toTraceString());
    }

    // ---- 2.6 circular queue ----

    private void showCircularQueue() {
        Console.say("A queue in a fixed array, bent into a ring. Watch the front and");
        Console.say("back markers walk off the end and come back to zero.");
        Console.say("This wrap is the evidence this structure owes the report.");
        Console.blank();

        CircularQueue<String> bays = new CircularQueue<>(4);

        bays.enqueue("AMB-1");
        Console.say("enqueue AMB-1: " + bays.toTraceString());
        bays.enqueue("AMB-2");
        Console.say("enqueue AMB-2: " + bays.toTraceString());
        bays.enqueue("AMB-3");
        Console.say("enqueue AMB-3: " + bays.toTraceString());

        Console.say("dequeue " + bays.dequeue() + ":  " + bays.toTraceString());
        Console.say("dequeue " + bays.dequeue() + ":  " + bays.toTraceString());

        bays.enqueue("AMB-4");
        Console.say("enqueue AMB-4: " + bays.toTraceString());
        bays.enqueue("AMB-5");
        Console.say("enqueue AMB-5: " + bays.toTraceString() + "   <-- back wrapped to 0");
        bays.enqueue("AMB-6");
        Console.say("enqueue AMB-6: " + bays.toTraceString());

        Console.blank();
        Console.say("The queue is now full. Adding one more:");
        try {
            bays.enqueue("AMB-7");
        } catch (Exception expected) {
            Console.say("  " + expected.getMessage());
        }
    }

    // ---- 2.7 deque ----

    private void showDeque() {
        Console.say("Push and pull at either end. A porter's job list: routine work");
        Console.say("joins the back, an urgent job jumps to the front.");
        Console.blank();

        LinkedDeque<String> jobs = new LinkedDeque<>();
        jobs.addLast("Q020 routine sample");
        jobs.addLast("Q021 routine sample");
        Console.say(jobs.toTraceString());

        jobs.addFirst("Q099 URGENT transfer");
        Console.say("after an urgent job jumps in: " + jobs.toTraceString());

        Console.blank();
        Console.say("next job taken: " + jobs.removeFirst());
        Console.say("last job dropped: " + jobs.removeLast());
        Console.say(jobs.toTraceString());
    }

    // ---- 2.8 binary search tree ----

    private void showBinarySearchTree() {
        Console.say("Smaller items go left, bigger ones go right. That keeps things");
        Console.say("sorted without ever running a sort.");
        Console.blank();

        BinarySearchTree<Integer> shuffled = new BinarySearchTree<>(BY_VALUE);
        int[] mixed = {50, 30, 70, 20, 40, 60, 80};
        for (int i = 0; i < mixed.length; i++) {
            shuffled.insert(Integer.valueOf(mixed[i]));
        }
        Console.say("mixed order:  " + shuffled.toTraceString());
        Console.say("looking for 40: " + shuffled.searchPath(Integer.valueOf(40)));

        Console.blank();
        Console.say("Now the same seven numbers, but already sorted:");
        BinarySearchTree<Integer> sorted = new BinarySearchTree<>(BY_VALUE);
        for (int i = 20; i <= 80; i += 10) {
            sorted.insert(Integer.valueOf(i));
        }
        Console.say("sorted order: " + sorted.toTraceString());
        Console.say("looking for 80: " + sorted.searchPath(Integer.valueOf(80)));

        Console.blank();
        Console.say("The second tree is a long thin chain, so searching it costs");
        Console.say("O(n) instead of O(log n). That is the problem the red-black");
        Console.say("tree fixes, and experiment E4 measures the difference.");
    }

    // ---- 2.9 chained hash table ----

    private void showChainedHashTable() {
        Console.say("Turns a key into a slot number. Two keys can land on the same");
        Console.say("slot, and this version hangs a small chain off each slot.");
        Console.blank();

        ChainedHashTable<String, String> cards = new ChainedHashTable<>(11);
        for (int i = 1; i <= 15; i++) {
            cards.put("CARD-" + i, "patient " + i);
        }

        Console.say(cards.toTraceString());
        Console.blank();
        Console.say("looking up CARD-7: " + cards.get("CARD-7"));
        Console.say("looking up CARD-99: " + cards.get("CARD-99") + " (not stored)");
        Console.blank();
        Console.say("Deliberately using only 11 slots for 15 keys, so collisions");
        Console.say("are guaranteed. Experiment E3 plots this properly.");
    }

    // ---- 2.10 set ----

    private void showSet() {
        Console.say("A bag where nothing appears twice. BFS and DFS use one to");
        Console.say("remember which places they have already visited.");
        Console.blank();

        HashBackedSet<String> visited = new HashBackedSet<>();
        Console.say("adding L001: " + visited.add("L001"));
        Console.say("adding L002: " + visited.add("L002"));
        Console.say("adding L001 again: " + visited.add("L001") + " (already there)");
        Console.say(visited.toTraceString());

        Console.blank();
        HashBackedSet<String> wards = new HashBackedSet<>();
        wards.add("L038");
        wards.add("L039");
        wards.add("L040");

        HashBackedSet<String> maternity = new HashBackedSet<>();
        maternity.add("L040");
        maternity.add("L041");
        maternity.add("L042");

        Console.say("wards:     " + wards);
        Console.say("maternity: " + maternity);
        Console.say("union:        " + wards.union(maternity));
        Console.say("intersection: " + wards.intersection(maternity));
        Console.say("difference:   " + wards.difference(maternity));
    }

    // ---- 2.11 heap ----

    private void showHeap() {
        Console.say("Always hands back the most urgent item first, without sorting");
        Console.say("the whole list. This is what decides who is seen next.");
        Console.blank();

        BinaryHeap<Integer> heap = new BinaryHeap<>(BY_VALUE);
        int[] arriving = {5, 3, 8, 1, 9, 2, 7};

        for (int i = 0; i < arriving.length; i++) {
            heap.insert(Integer.valueOf(arriving[i]));
            Console.say("insert " + arriving[i] + ": " + heap.toTraceString());
        }

        Console.blank();
        Console.say("taking them out, most urgent first:");
        StringBuilder order = new StringBuilder();
        while (!heap.isEmpty()) {
            order.append(heap.extract()).append(" ");
        }
        Console.say("  " + order);
        Console.say("They come out sorted, even though we never ran a sort.");
    }

    // ---- 2.12 red-black tree ----

    private void showRedBlackTree() {
        Console.say("A search tree that rebalances itself. Fed sorted numbers, which");
        Console.say("would turn a plain tree into a long chain.");
        Console.blank();

        RedBlackTree<Integer> tree = new RedBlackTree<>(BY_VALUE);
        for (int i = 1; i <= 15; i++) {
            tree.insert(Integer.valueOf(i));
        }

        Console.say(tree.toTraceString());
        Console.blank();
        Console.say("A plain tree given the same 15 sorted numbers would be 15 levels");
        Console.say("deep. This one is " + tree.height() + ".");
        Console.blank();
        Console.say("Every rotation, with the shape before it:");
        Console.say(tree.getRotationLog());
    }

    // ---- 2.13 B-tree ----

    private void showBTree() {
        Console.say("Each node holds several keys, not just one, so the tree stays");
        Console.say("shallow. This is how database indexes work, including MySQL's.");
        Console.blank();

        BTree<Integer> tree = new BTree<>(5, BY_VALUE);
        for (int i = 1; i <= 20; i++) {
            tree.insert(Integer.valueOf(i));
        }

        Console.say(tree.toTraceString());
        Console.blank();
        Console.say("Every node split, which is the interesting part:");
        Console.say(tree.getSplitLog());
    }

    // ---- 2.14 map ----

    private void showMap() {
        Console.say("Look something up by its key. The whole program leans on this.");
        Console.blank();

        HashBackedMap<String, String> places = new HashBackedMap<>();
        places.put("L001", "Main Gate");
        places.put("L015", "A and E Triage Point");
        places.put("L040", "Maternity Wing Entrance");

        Console.say(places.toString());
        Console.blank();
        Console.say("L015 is " + places.get("L015"));
        Console.say("L999 is " + places.get("L999") + " (no such place)");
        Console.blank();
        Console.say("Table size comes from setting P1, currently "
                + Parameters.getHashTableSize() + ".");
        Console.say("Menu option 8 can change that while the program runs.");
    }

    // ---- 2.15 disjoint set ----

    private void showDisjointSet() {
        Console.say("Keeps track of which things are joined. Kruskal uses it to ask");
        Console.say("'would adding this path make a loop?'");
        Console.blank();

        DisjointSet<String> groups = new DisjointSet<>();
        String[] places = {"L001", "L002", "L015", "L040", "L041"};
        for (int i = 0; i < places.length; i++) {
            groups.makeSet(places[i]);
        }
        Console.say("everyone starts alone: " + groups.toTraceString());

        Console.say("join L001 and L002: " + groups.union("L001", "L002"));
        Console.say("  " + groups.toTraceString());
        Console.say("join L040 and L041: " + groups.union("L040", "L041"));
        Console.say("  " + groups.toTraceString());
        Console.say("join L001 and L002 again: " + groups.union("L001", "L002")
                + "   <-- false, they are already together, so this would be a loop");
        Console.blank();
        Console.say("are L001 and L040 connected? " + groups.connected("L001", "L040"));
        Console.say("are L001 and L002 connected? " + groups.connected("L001", "L002"));
    }

    // ---- 2.17 both graphs ----

    private void showGraphs() {
        if (!session.requireLoaded()) {
            return;
        }
        Console.say("The same hospital map held two different ways. The brief asks");
        Console.say("for both, and this is where the difference shows.");
        Console.blank();

        Console.say(session.getListMap().toTraceString());
        Console.blank();
        Console.say(session.getMatrixMap().toTraceString());
        Console.blank();
        Console.say("Same places, same paths, same answers. The list stores only the");
        Console.say("paths that exist, so it is small. The grid stores a slot for");
        Console.say("every possible pair, so most of it is empty, but asking 'is");
        Console.say("there a path from here to there' is one lookup instead of a scan.");
    }
}
