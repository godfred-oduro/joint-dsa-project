# Who wrote what

Owner: 22248029. Update this table as files are finished.

The group is 16. It was 17 at the start; the departed member's queue and
request handling went to 22367946, and the breadth-first search to 22372639,
who now owns both graph traversals.




| Index number | Data structure | Algorithm | Hospital rule | Database file | Other files |
|---|---|---|---|---|---|
| 22248029 | graph: GraphView, AdjacencyListGraph, AdjacencyMatrixGraph | Dijkstra | — | — | pom.xml, .gitignore, CODEOWNERS, this file, Parameters, all menu classes, integration tests |
| 22040255 | RedBlackTree | Kruskal | — | — | the whole exception package, Haversine |
| 22104879 | BTree | Prim | — | — | development-log.md, config.properties.example, locations.csv, roads.csv |
| 11102882 | DisjointSet | KnapsackFill | BudgetSelector | — | TriageScorer |
| 22224236 | BinaryHeap | KnapsackReconstruct | TriageQueue | AlgorithmRunDao* | AlgorithmResult, TimingHarness, ExperimentRunner, CsvExporter, BenchmarkMenu, docs/graphs |
| 22015166 | LinkedQueue | TopologicalSort | ReferralOrdering | ConnectionManager | schema.sql, service_requests.csv, RequestGenerator |
| 22160537 | HashBackedMap | GreedyAssignment | ClinicDeferral, Dispatcher | — | ClinicSchedule |
| 22399601 | DynamicArray | BreadthFirstSearch | — | LocationDao | Location |
| 22392509 | SinglyLinkedList | BinarySearch | — | RoadDao | Road |
| 22372680 | DoublyLinkedList | SelectionSort | — | — | docs/attendance |
| 22372639 | LinkedStack | DepthFirstSearch | UndoService | — | AuditMenu |
| 22395395 | CircularQueue | QuickSort | — | ResourceDao | Resource, resources.csv |
| 22367946 | LinkedDeque | MergeSort | — | ServiceRequestDao | TablePrinter, ServiceRequest |
| 22305896 | BinarySearchTree | LinearSearch | — | AlgorithmRunDao | AlgorithmRun |
| 22370298 | ChainedHashTable | InsertionSort | — | AuditEventDao | AuditEvent |
| 22399010 | HashBackedSet | BruteForceRoutes | — | — | TraceRow, EvidenceMenu, TestMenu, README, docs/traces, docs/proofs |

\* AlgorithmRunDao belongs to 22305896, who owns the AlgorithmRun model.
22224236 uses it from the timing code but does not own the file.

## Proof that everyone took part

1. `.github/CODEOWNERS` makes GitHub ask the owner to review any change to
   their file. Nobody can quietly edit someone else's work.
2. Every file starts with a comment naming its owner's index number.
3. This table.
4. `git shortlog -sn` gives a commit count per member. Put it in report
   section 11.
