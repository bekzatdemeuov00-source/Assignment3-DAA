package daa;

import daa.algorithms.StrongComponents;
import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class SccTest {
    @Test void randomPartitionsMatchAllPairsBfsInBothDirections() {
        Random random = new Random(2026);
        for (int trial = 0; trial < 50; trial++) {
            int n = trial == 49 ? 200 : 1 + random.nextInt(100);
            Graph graph = new Graph(n, true);
            for (int e = 0; e < trial % 6 * n; e++)
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt());
            boolean[][] reachable = new boolean[n][n];
            for (int start = 0; start < n; start++) {
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                queue.add(start);
                reachable[start][start] = true;
                while (!queue.isEmpty()) {
                    int v = queue.remove();
                    for (Graph.Edge edge : graph.adjacent(v)) if (!reachable[start][edge.to()]) {
                        reachable[start][edge.to()] = true;
                        queue.add(edge.to());
                    }
                }
            }
            Metrics k = new Metrics(), t = new Metrics();
            var kosaraju = StrongComponents.kosaraju(graph, k);
            var tarjan = StrongComponents.tarjan(graph, t);
            int bruteCount = 0;
            for (int a = 0; a < n; a++) {
                boolean first = true;
                for (int b = 0; b < n; b++) {
                    boolean same = reachable[a][b] && reachable[b][a];
                    assertEquals(same, kosaraju.componentOf(a) == kosaraju.componentOf(b));
                    assertEquals(same, tarjan.componentOf(a) == tarjan.componentOf(b));
                    if (b < a && same) first = false;
                }
                if (first) bruteCount++;
            }
            assertEquals(bruteCount, kosaraju.count());
            assertEquals(bruteCount, tarjan.count());
            assertTrue(StrongComponents.samePartition(kosaraju, tarjan));
            assertEquals(2L * graph.m(), k.edgeScans);
            assertEquals(graph.m(), t.edgeScans);
        }
    }

    @Test void oneHundredThousandDeepPathAndCycleAreStackSafe() {
        Graph graph = new Graph(100_000, true);
        for (int v = 1; v < graph.n(); v++) graph.addEdge(v - 1, v, 1);
        for (boolean cycle : new boolean[]{false, true}) {
            if (cycle) graph.addEdge(graph.n() - 1, 0, 1);
            Metrics k = new Metrics(), t = new Metrics();
            var a = StrongComponents.kosaraju(graph, k);
            var b = StrongComponents.tarjan(graph, t);
            assertEquals(cycle ? 1 : graph.n(), a.count());
            assertTrue(StrongComponents.samePartition(a, b));
            assertEquals(graph.n(), k.maxDepth);
            assertEquals(graph.n(), t.maxDepth);
        }
    }

    @Test void sampleLoopsParallelEdgesEmptyAndIsolatedVertices() throws Exception {
        Graph sample = Graph.readFile(Path.of("data/cyclic.txt"));
        assertEquals(2, StrongComponents.kosaraju(sample, new Metrics()).count());
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 0, 0);
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 0, 4);
        assertEquals(3, StrongComponents.tarjan(graph, new Metrics()).count());
        assertTrue(StrongComponents.samePartition(StrongComponents.kosaraju(graph, new Metrics()),
                StrongComponents.tarjan(graph, new Metrics())));
        assertEquals(0, StrongComponents.kosaraju(new Graph(0, true), new Metrics()).count());
        assertEquals(0, StrongComponents.tarjan(new Graph(0, true), new Metrics()).count());
        assertThrows(IllegalArgumentException.class,
                () -> StrongComponents.kosaraju(new Graph(2, false), new Metrics()));
        assertThrows(IllegalArgumentException.class,
                () -> StrongComponents.tarjan(new Graph(2, false), new Metrics()));
    }
}
