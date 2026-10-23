package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class PathsTest {
    @Test void shortestAndLongestMatchBellmanFordOnRandomNegativeDags() {
        Random random = new Random(903);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + random.nextInt(50);
            List<Integer> permutation = new ArrayList<>();
            for (int v = 0; v < n; v++) permutation.add(v);
            Collections.shuffle(permutation, random);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++)
                if (random.nextDouble() < .13)
                    graph.addEdge(permutation.get(i), permutation.get(j), random.nextInt(101) - 50);
            assertTrue(TopologicalSort.isValid(graph, TopologicalSort.kahn(graph, new Metrics())));
            for (int sign : new int[]{1, -1}) {
                Metrics metrics = new Metrics();
                var actual = sign == 1 ? DagPaths.shortest(graph, metrics) : DagPaths.longest(graph, metrics);
                assertArrayEquals(bellmanFord(graph, sign), actual.distances());
                assertEquals(graph.m(), metrics.edgeScans);
                for (int v = 0; v < n; v++) assertRestoredPath(graph, actual, v);
            }
        }
    }

    @Test void examplesAndEverySourceIncludingIsolates() throws Exception {
        Graph sample = Graph.readFile(Path.of("data/dag.txt"));
        assertArrayEquals(new long[]{0, 5, 3, -1, 0}, DagPaths.shortest(sample, new Metrics()).distances());
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 2, 10);
        graph.addEdge(1, 2, -5);
        graph.addEdge(2, 3, 2);
        graph.addEdge(2, 3, 3);
        graph.addEdge(3, 4, Integer.MIN_VALUE);
        var paths = DagPaths.shortest(graph, new Metrics());
        assertEquals(-3L + Integer.MIN_VALUE, paths.distanceTo(4));
        assertEquals(List.of(1, 2, 3, 4), paths.pathTo(4));
        assertEquals(List.of(5), paths.pathTo(5));
        assertEquals(0, paths.distanceTo(5));
        assertEquals(List.of(), TopologicalSort.kahn(new Graph(0, true), new Metrics()));
        assertEquals(0, DagPaths.shortest(new Graph(0, true), new Metrics()).sum());
    }

    @Test void cyclesAndInvalidOrdersAreRejected() throws Exception {
        Graph cyclic = Graph.readFile(Path.of("data/cyclic.txt"));
        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(cyclic, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> DagPaths.shortest(cyclic, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> DagPaths.longest(cyclic, new Metrics()));
        Graph loop = new Graph(1, true);
        loop.addEdge(0, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(loop, new Metrics()));
        Graph dag = new Graph(2, true);
        dag.addEdge(0, 1, 1);
        for (List<Integer> order : List.of(List.of(0), List.of(0, 0), List.of(1, 0), List.of(0, 2)))
            assertFalse(TopologicalSort.isValid(dag, order));
        assertThrows(IllegalArgumentException.class, () -> TopologicalSort.kahn(new Graph(2, false), new Metrics()));
    }

    @Test void condensationRemovesCyclesAndCombinesParallelTransitions() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 2); graph.addEdge(1, 0, 3);
        graph.addEdge(1, 2, 4); graph.addEdge(0, 2, 7);
        graph.addEdge(2, 3, 1); graph.addEdge(3, 2, 1);
        graph.addEdge(3, 4, 6); graph.addEdge(4, 5, 2);
        var scc = StrongComponents.kosaraju(graph, new Metrics());
        Graph condensed = Condensation.build(graph, scc);
        assertEquals(4, condensed.n());
        assertEquals(3, condensed.m());
        assertTrue(TopologicalSort.isValid(condensed, TopologicalSort.kahn(condensed, new Metrics())));
        var critical = DagPaths.longest(condensed, new Metrics());
        assertEquals(15, critical.distanceTo(scc.componentOf(5)));
        assertEquals(List.of(scc.componentOf(0), scc.componentOf(2), scc.componentOf(4), scc.componentOf(5)),
                critical.pathTo(scc.componentOf(5)));
    }

    @Test void randomCondensationsAreDagsAndLongestMatchesExhaustivePaths() {
        Random random = new Random(71);
        for (int trial = 0; trial < 80; trial++) {
            Graph graph = new Graph(8, true);
            for (int e = 0; e < trial % 20; e++)
                graph.addEdge(random.nextInt(8), random.nextInt(8), random.nextInt(11) - 5);
            Graph dag = Condensation.build(graph, StrongComponents.tarjan(graph, new Metrics()));
            assertTrue(TopologicalSort.isValid(dag, TopologicalSort.kahn(dag, new Metrics())));
            long[] best = new long[dag.n()];
            Arrays.fill(best, Long.MIN_VALUE);
            int[] degree = dag.inDegrees();
            for (int v = 0; v < dag.n(); v++) if (degree[v] == 0) enumerate(dag, v, 0, best);
            assertArrayEquals(best, DagPaths.longest(dag, new Metrics()).distances());
        }
    }

    private static void enumerate(Graph graph, int v, long cost, long[] best) {
        best[v] = Math.max(best[v], cost);
        for (Graph.Edge edge : graph.adjacent(v)) enumerate(graph, edge.to(), cost + edge.weight(), best);
    }

    private static long[] bellmanFord(Graph graph, int sign) {
        long[] distance = new long[graph.n()];
        Arrays.fill(distance, Long.MAX_VALUE);
        int[] indegree = graph.inDegrees();
        for (int v = 0; v < graph.n(); v++) if (indegree[v] == 0) distance[v] = 0;
        for (int pass = 1; pass < graph.n(); pass++) {
            for (Graph.Edge edge : graph.edges()) {
                if (distance[edge.from()] != Long.MAX_VALUE)
                    distance[edge.to()] = Math.min(distance[edge.to()],
                            distance[edge.from()] + (long) sign * edge.weight());
            }
        }
        for (int v = 0; v < graph.n(); v++) distance[v] *= sign;
        return distance;
    }

    private static void assertRestoredPath(Graph graph, DagPaths.Result paths, int target) {
        List<Integer> path = paths.pathTo(target);
        assertEquals(target, path.get(path.size() - 1));
        assertEquals(0, graph.inDegrees()[path.get(0)]);
        assertEquals(path.size(), path.stream().distinct().count());
        long weight = 0;
        for (int i = 1; i < path.size(); i++) {
            int from = path.get(i - 1), to = path.get(i);
            long delta = paths.distanceTo(to) - paths.distanceTo(from);
            assertTrue(graph.adjacent(from).stream().anyMatch(e -> e.to() == to && e.weight() == delta));
            assertEquals(from, paths.parentOf(to));
            weight += delta;
        }
        assertEquals(paths.distanceTo(target), weight);
    }
}
