# Assignment 3 — Graphs: DSU, MST, SCC and Shortest Paths

## 1. Overview

The project implements six graph operations using a shared adjacency-list representation. Undirected graphs use Kruskal and Prim to obtain a minimum spanning forest; directed graphs use iterative Kosaraju, Kahn topological ordering and shortest paths on a DAG. DSU uses both union by size and path compression. The graph parser accepts the required `n m directed|undirected` text format.

## 2. Complexity

| Algorithm | Time | Extra space | Reason |
|---|---|---|---|
| DSU find/union | amortized O(α(V)) | O(V) | Compression and size heuristic keep trees shallow |
| Kruskal | O(E log E + E α(V)) | O(V+E) | Edge sorting dominates |
| Prim (heap) | O(E log E) | O(V+E) | Heap offers and polls |
| Kosaraju | O(V+E) | O(V+E) | Two DFS traversals plus reverse graph |
| Kahn | O(V+E) | O(V) | Every vertex processed once and each edge scanned |
| DAG shortest paths | O(V+E) | O(V) | Topological sort and each edge relaxed once |

## 3. Correctness — MST and the cut property

A cut partitions the vertices into two sets. A minimum-weight edge crossing the cut is safe to add to a minimum spanning tree. Kruskal processes edges in nondecreasing weight order and accepts an edge only if it connects two different DSU components. Therefore each accepted edge is a safe connection between existing components. Prim similarly selects the minimum-weight edge that reaches an unvisited vertex from the current tree. Both algorithms avoid cycles and add the smallest possible safe connections. For disconnected graphs, repeating this process produces one minimum spanning tree per connected component. The program cross-checks both implementations and rejects any disagreement in weight.

## 4. Correctness — DAG shortest paths

In a topological order every incoming neighbor of a vertex occurs before that vertex. Initially each source (in-degree zero) has distance zero, and other vertices are assigned infinity. Assume that distances are correct for all previously processed vertices. For the next vertex, all incoming paths must end through an already processed predecessor, so the earlier relaxations already provided its minimum distance. Relaxing this vertex's outgoing edges then propagates the correct candidate distances to later vertices. By induction, after one pass all distances are correct. Negative edge weights are safe because a DAG has no directed cycles and therefore cannot have negative cycles. Parent pointers retain the chosen path.

## 5. Which algorithm when?

| Situation | Choice | Why |
|---|---|---|
| Undirected edge list, MST needed | Kruskal | Simple sorted edges + DSU |
| Undirected adjacency list, MST needed | Prim | Naturally expands frontier via heap |
| Disconnected undirected graph | Either | Both return minimum spanning forest |
| Directed graph, strongly connected regions | Kosaraju | Two linear-time passes |
| Directed dependency graph, order needed | Kahn | Gives ordering or reports a cycle |
| DAG pipeline, shortest routes | DAG shortest paths | Linear time, even with negative weights |
| Directed cyclic graph with negative edges | Not DAG shortest paths | Topological order does not exist; use an appropriate general shortest-path method |

## 6. Benchmark and operation counters

The benchmark uses five vertex counts (1,000; 3,162; 10,000; 31,623; 100,000), with 4V and 20V edges, and three graph kinds at each setting. Its CSV has five algorithm rows per setting, 50 data rows total. Metrics are counted inside algorithms, not inferred from theoretical formulas. Each result is run five times and the median milliseconds recorded. Measurements are hardware- and JIT-dependent, so operation counts provide the main comparison.

For Kosaraju, `edgeScans/m = 2` because it visits every edge in both passes. For Kahn and the DAG relaxation, `edgeScans/m = 1`. Those exact identities agree with the linear-time expectation; equivalently `edgeScans/(V+E)` remains bounded. Kruskal uses E scans of sorted edges plus DSU calls and O(E log E) weight comparisons. Prim scans each undirected adjacency entry, so `edgeScans/m = 2`. As E grows proportionally to V, the scan curves have a log-log slope of 1. Kruskal's comparison count grows slightly faster than linear because sorting adds a log factor.

**Reference validation limitation:** The seeded generator is deterministic but its MST totals differ from the exact reference table in the instructions; this remains an unresolved discrepancy in generation details. The output should not be presented as successfully matching reference values. The program currently checks Kruskal/Prim agreement and DAG ordering, but does not assert the provided reference-table values.

## 7. Plots

![Operation counts](results/plots/operations.png)

![Scans per edge](results/plots/scans_per_edge.png)

![Kruskal edge share](results/plots/kruskal_share.png)

## 8. Testing

The JUnit 5 suite covers DSU reference checks, MST agreement on 100 connected random graphs, disconnected forest behavior, SCC against brute-force reachability, valid and cyclic topological sorts, DAG shortest paths against Bellman-Ford with negative weights, sample data files and a 100,000-vertex stack-safety test. The Java source and benchmark were compiled/executed in OpenJDK 21. JUnit tests were not executed in the build environment because Maven was unavailable.
