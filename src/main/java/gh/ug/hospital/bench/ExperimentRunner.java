package gh.ug.hospital.bench;

import gh.ug.hospital.algo.BinarySearch;
import gh.ug.hospital.algo.BreadthFirstSearch;
import gh.ug.hospital.algo.DepthFirstSearch;
import gh.ug.hospital.algo.Dijkstra;
import gh.ug.hospital.algo.InsertionSort;
import gh.ug.hospital.algo.Kruskal;
import gh.ug.hospital.algo.LinearSearch;
import gh.ug.hospital.algo.MergeSort;
import gh.ug.hospital.algo.QuickSort;
import gh.ug.hospital.algo.SelectionSort;
import gh.ug.hospital.ds.BinaryHeap;
import gh.ug.hospital.ds.BinarySearchTree;
import gh.ug.hospital.ds.ChainedHashTable;
import gh.ug.hospital.ds.RedBlackTree;
import gh.ug.hospital.ds.graph.AdjacencyListGraph;
import gh.ug.hospital.ds.graph.GraphView;
import gh.ug.hospital.util.AlgorithmResult;
import gh.ug.hospital.util.Parameters;

import java.util.Comparator;
import java.util.Random;

/**
 * Owner: 22224236
 *
 * Runs the six timing experiments the brief asks for.
 *
 *   E1  linear search against binary search
 *   E2  the four sorting methods compared
 *   E3  hash table fullness against collisions
 *   E4  plain search tree against balanced tree
 *   E5  heap insert and remove
 *   E6  the four graph algorithms
 *
 * The test data is built with java.util.Random using seed P2, which came
 * from our index numbers. That means the same numbers come out every time
 * anyone runs it, so the measurements can be repeated exactly. Random is a
 * number generator, not a collection, so it does not break constraint C19.
 */
public class ExperimentRunner {

    /** Numbers compared the ordinary way. */
    private static final Comparator<Integer> BY_VALUE = new Comparator<Integer>() {
        @Override
        public int compare(Integer a, Integer b) {
            return a.compareTo(b);
        }
    };

    private final TimingHarness harness;

    public ExperimentRunner(TimingHarness harness) {
        this.harness = harness;
    }

    // =================================================================
    // E1: linear search against binary search
    // =================================================================

    public static final int[] E1_SIZES = {100, 500, 1000, 5000, 10000};

    public void runE1() {
        for (int s = 0; s < E1_SIZES.length; s++) {
            final int size = E1_SIZES[s];
            final Integer[] sorted = sortedNumbers(size);

            // Look for the very last item, which is the worst case for linear
            // search. Anything else would flatter it.
            final Integer target = sorted[size - 1];

            harness.measure("E1", "LinearSearch", null, size, new TimingHarness.Timeable() {
                @Override
                public AlgorithmResult runOnce() {
                    return LinearSearch.run(sorted, target, BY_VALUE);
                }
            });

            harness.measure("E1", "BinarySearch", null, size, new TimingHarness.Timeable() {
                @Override
                public AlgorithmResult runOnce() {
                    return BinarySearch.searchWithoutChecking(sorted, target, BY_VALUE);
                }
            });
        }
    }

    // =================================================================
    // E2: the four sorting methods
    // =================================================================

    public static final int[] E2_SIZES = {100, 500, 1000, 5000, 10000};

    public void runE2() {
        for (int s = 0; s < E2_SIZES.length; s++) {
            final int size = E2_SIZES[s];

            // Each sort gets its own fresh copy of the same shuffled numbers,
            // otherwise the second one would be handed data already sorted by
            // the first, which would be nonsense.

            harness.measure("E2", "SelectionSort", null, size, new TimingHarness.Timeable() {
                @Override
                public AlgorithmResult runOnce() {
                    return SelectionSort.run(shuffledNumbers(size), BY_VALUE);
                }
            });

            harness.measure("E2", "InsertionSort", null, size, new TimingHarness.Timeable() {
                @Override
                public AlgorithmResult runOnce() {
                    return InsertionSort.run(shuffledNumbers(size), BY_VALUE);
                }
            });

            harness.measure("E2", "MergeSort", null, size, new TimingHarness.Timeable() {
                @Override
                public AlgorithmResult runOnce() {
                    return MergeSort.run(shuffledNumbers(size), BY_VALUE);
                }
            });

            harness.measure("E2", "QuickSort", null, size, new TimingHarness.Timeable() {
                @Override
                public AlgorithmResult runOnce() {
                    return QuickSort.run(shuffledNumbers(size), BY_VALUE);
                }
            });
        }
    }

    // =================================================================
    // E3: hash table fullness against collisions
    // =================================================================

    public static final int[] E3_SIZES = {100, 1000, 5000, 10000, 20000};

    /**
     * Fills the hash table with more and more keys and records how long it
     * takes.
     *
     * The collision count is the real point of this experiment. As the table
     * fills up, more keys land on a slot that is already in use, the chains
     * get longer, and each lookup has further to walk.
     */
    public void runE3() {
        for (int s = 0; s < E3_SIZES.length; s++) {
            final int size = E3_SIZES[s];

            // Keep the table size fixed so that fullness really does change.
            final int slots = Parameters.getHashTableSize();

            harness.measure("E3", "ChainedHashTable", "ChainedHashTable", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            AlgorithmResult result = new AlgorithmResult();
                            long started = System.nanoTime();

                            ChainedHashTable<String, Integer> table =
                                    new ChainedHashTable<>(slots);
                            for (int i = 0; i < size; i++) {
                                table.put("KEY-" + i, Integer.valueOf(i));
                            }

                            result.setElapsedNanos(System.nanoTime() - started);
                            result.addComparisons(table.getCollisions());
                            result.setAnswer(table);
                            return result;
                        }
                    });

        }
    }

    // =================================================================
    // E4: plain search tree against balanced tree
    // =================================================================

    public static final int[] E4_SIZES = {100, 1000, 5000, 10000};

    /**
     * Three cases, and the middle one is the whole point.
     *
     * A plain tree given shuffled numbers stays reasonably short. The same
     * tree given already sorted numbers turns into a long thin chain, because
     * every item goes to the right of the one before. The red-black tree
     * given the same sorted numbers stays short, because it rebalances.
     */
    public void runE4() {
        for (int s = 0; s < E4_SIZES.length; s++) {
            final int size = E4_SIZES[s];

            harness.measure("E4", "BST-shuffled", "BinarySearchTree", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return buildPlainTree(shuffledNumbers(size));
                        }
                    });

            harness.measure("E4", "BST-sorted", "BinarySearchTree", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return buildPlainTree(sortedNumbers(size));
                        }
                    });

            harness.measure("E4", "RedBlackTree-sorted", "RedBlackTree", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return buildBalancedTree(sortedNumbers(size));
                        }
                    });
        }
    }

    private AlgorithmResult buildPlainTree(Integer[] numbers) {
        AlgorithmResult result = new AlgorithmResult();
        long started = System.nanoTime();

        BinarySearchTree<Integer> tree = new BinarySearchTree<>(BY_VALUE);
        for (int i = 0; i < numbers.length; i++) {
            tree.insert(numbers[i]);
        }
        // Look something up, so the measurement covers searching too.
        tree.search(numbers[numbers.length / 2]);

        result.setElapsedNanos(System.nanoTime() - started);
        result.addComparisons(tree.getComparisons());
        result.setAnswer(Integer.valueOf(tree.height()));
        return result;
    }

    private AlgorithmResult buildBalancedTree(Integer[] numbers) {
        AlgorithmResult result = new AlgorithmResult();

        RedBlackTree<Integer> tree = new RedBlackTree<>(BY_VALUE);

        // Off for timing. With it on, every rotation pays to walk and
        // stringify the whole tree, which is what was making this look far
        // slower than the plain BST it is supposed to be beating. See the
        // note on RedBlackTree.loggingEnabled.
        tree.setLoggingEnabled(false);

        long started = System.nanoTime();
        for (int i = 0; i < numbers.length; i++) {
            tree.insert(numbers[i]);
        }
        tree.search(numbers[numbers.length / 2]);

        result.setElapsedNanos(System.nanoTime() - started);
        result.addComparisons(tree.getComparisons());
        result.addSwaps(tree.getRotations());
        result.setAnswer(Integer.valueOf(tree.height()));
        return result;
    }

    // =================================================================
    // E5: heap insert and remove
    // =================================================================

    public static final int[] E5_SIZES = {100, 1000, 5000, 10000, 20000};

    public void runE5() {
        for (int s = 0; s < E5_SIZES.length; s++) {
            final int size = E5_SIZES[s];

            harness.measure("E5", "HeapInsert", "BinaryHeap", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            AlgorithmResult result = new AlgorithmResult();
                            Integer[] numbers = shuffledNumbers(size);

                            long started = System.nanoTime();
                            BinaryHeap<Integer> heap = new BinaryHeap<>(BY_VALUE);
                            for (int i = 0; i < numbers.length; i++) {
                                heap.insert(numbers[i]);
                            }
                            result.setElapsedNanos(System.nanoTime() - started);

                            result.addSwaps(heap.getSwapCount());
                            result.setAnswer(heap);
                            return result;
                        }
                    });

            harness.measure("E5", "HeapExtract", "BinaryHeap", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            AlgorithmResult result = new AlgorithmResult();

                            // Build it first, outside the clock. We are timing
                            // taking things out, not putting them in.
                            BinaryHeap<Integer> heap = new BinaryHeap<>(BY_VALUE);
                            Integer[] numbers = shuffledNumbers(size);
                            for (int i = 0; i < numbers.length; i++) {
                                heap.insert(numbers[i]);
                            }

                            long started = System.nanoTime();
                            while (!heap.isEmpty()) {
                                heap.extract();
                            }
                            result.setElapsedNanos(System.nanoTime() - started);

                            result.addSwaps(heap.getSwapCount());
                            return result;
                        }
                    });
        }
    }

    // =================================================================
    // E6: the four graph algorithms
    // =================================================================

    public static final int[] E6_SIZES = {50, 100, 200, 350, 500};

    public void runE6() {
        for (int s = 0; s < E6_SIZES.length; s++) {
            final int size = E6_SIZES[s];
            final GraphView graph = buildTestGraph(size);
            final String start = "P0";

            harness.measure("E6", "BreadthFirstSearch", "AdjacencyListGraph", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return BreadthFirstSearch.run(graph, start);
                        }
                    });

            harness.measure("E6", "DepthFirstSearch", "AdjacencyListGraph", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return DepthFirstSearch.run(graph, start);
                        }
                    });

            harness.measure("E6", "Dijkstra", "AdjacencyListGraph", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return Dijkstra.run(graph, start, "P" + (size - 1));
                        }
                    });

            harness.measure("E6", "Kruskal", "DisjointSet", size,
                    new TimingHarness.Timeable() {
                        @Override
                        public AlgorithmResult runOnce() {
                            return Kruskal.run(graph);
                        }
                    });
        }
    }

    /**
     * Builds a test map of a given size.
     *
     * Every place is joined to the next one, which guarantees the graph is
     * connected. Then a few extra paths are added at random so it is not just
     * a straight line. The seed makes it come out the same every time.
     */
    private GraphView buildTestGraph(int places) {
        AdjacencyListGraph graph = new AdjacencyListGraph();
        Random dice = new Random(Parameters.getRandomSeed());

        for (int i = 0; i < places; i++) {
            graph.addVertex("P" + i);
        }
        for (int i = 0; i < places - 1; i++) {
            graph.addEdge("P" + i, "P" + (i + 1), 1.0 + dice.nextDouble() * 4.0);
        }

        // About two extra paths per place, which is roughly the shape of the
        // real hospital map.
        int extras = places * 2;
        for (int i = 0; i < extras; i++) {
            int a = dice.nextInt(places);
            int b = dice.nextInt(places);
            if (a != b) {
                graph.addEdge("P" + a, "P" + b, 1.0 + dice.nextDouble() * 4.0);
            }
        }
        return graph;
    }

    // =================================================================
    // test data
    // =================================================================

    /** Shuffled numbers, the same every time thanks to seed P2. */
    private Integer[] shuffledNumbers(int howMany) {
        Integer[] numbers = new Integer[howMany];
        for (int i = 0; i < howMany; i++) {
            numbers[i] = Integer.valueOf(i);
        }

        Random dice = new Random(Parameters.getRandomSeed());
        for (int i = howMany - 1; i > 0; i--) {
            int j = dice.nextInt(i + 1);
            Integer hold = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = hold;
        }
        return numbers;
    }

    /** Numbers already in order, which is the worst case for a plain tree. */
    private Integer[] sortedNumbers(int howMany) {
        Integer[] numbers = new Integer[howMany];
        for (int i = 0; i < howMany; i++) {
            numbers[i] = Integer.valueOf(i);
        }
        return numbers;
    }

    /** Runs all six, one after another. Menu option M5.0. */
    public void runAll() {
        runE1();
        runE2();
        runE3();
        runE4();
        runE5();
        runE6();
    }
}
