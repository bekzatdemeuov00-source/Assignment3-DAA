package daa;

import daa.algorithms.MinimumSpanningForest;
import daa.algorithms.MinimumSpanningForest.Result;
import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class MstTest {
    @Test void oneHundredConnectedGraphsAgree() {
        Random random = new Random(2206);
        for (int test = 0; test < 100; test++) {
            int n = 2 + random.nextInt(80);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) graph.addEdge(v, random.nextInt(v), random.nextInt(201) - 100);
            for (int edge = 0; edge < 4 * n; edge++)
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(201) - 100);
            check(graph, n - 1, 1);
        }
    }

    @Test void tinyGraphsMatchExhaustiveSpanningTrees() {
        Random random = new Random(58);
        for (int test = 0; test < 50; test++) {
            int n = 2 + random.nextInt(5);
            Graph graph = new Graph(n, false);
            for (int v = 1; v < n; v++) graph.addEdge(v - 1, v, random.nextInt(21) - 10);
            while (graph.m() < 10)
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(21) - 10);
            long best = Long.MAX_VALUE;
            for (int mask = 0; mask < (1 << graph.m()); mask++) {
                if (Integer.bitCount(mask) != n - 1) continue;
                int[] labels = new int[n];
                for (int i = 0; i < n; i++) labels[i] = i;
                boolean cycle = false;
                long weight = 0;
                for (int i = 0; i < graph.m(); i++) {
                    if ((mask & (1 << i)) == 0) continue;
                    Graph.Edge edge = graph.edges().get(i);
                    int old = labels[edge.to()], replacement = labels[edge.from()];
                    if (old == replacement) { cycle = true; break; }
                    for (int v = 0; v < n; v++) if (labels[v] == old) labels[v] = replacement;
                    weight += edge.weight();
                }
                if (!cycle) best = Math.min(best, weight);
            }
            assertEquals(best, MinimumSpanningForest.kruskal(graph, new Metrics()).weight());
            assertEquals(best, MinimumSpanningForest.prim(graph, new Metrics()).weight());
        }
    }

    @Test void disconnectedExampleAndEdgeCases() throws Exception {
        Graph example = Graph.readFile(Path.of("data/disconnected.txt"));
        check(example, 2, 3);
        assertEquals(8, MinimumSpanningForest.kruskal(example, new Metrics()).weight());
        Graph graph = new Graph(6, false);
        graph.addEdge(0, 0, Integer.MIN_VALUE);
        graph.addEdge(0, 1, 7);
        graph.addEdge(0, 1, -5);
        graph.addEdge(2, 3, Integer.MAX_VALUE);
        graph.addEdge(3, 4, Integer.MAX_VALUE);
        check(graph, 3, 3);
        assertEquals(2L * Integer.MAX_VALUE - 5, MinimumSpanningForest.prim(graph, new Metrics()).weight());
        check(new Graph(0, false), 0, 0);
        check(new Graph(1, false), 0, 1);
        check(new Graph(5, false), 0, 5);
    }

    @Test void directedInputIsRejected() {
        Graph directed = new Graph(2, true);
        assertThrows(IllegalArgumentException.class, () -> MinimumSpanningForest.kruskal(directed, new Metrics()));
        assertThrows(IllegalArgumentException.class, () -> MinimumSpanningForest.prim(directed, new Metrics()));
    }

    private static void check(Graph graph, int edgeCount, int components) {
        Metrics kruskal = new Metrics(), prim = new Metrics();
        Result a = MinimumSpanningForest.kruskal(graph, kruskal);
        Result b = MinimumSpanningForest.prim(graph, prim);
        assertEquals(a.weight(), b.weight());
        for (Result result : new Result[]{a, b}) {
            assertEquals(edgeCount, result.edges().size());
            assertEquals(components, result.components());
            assertEquals(graph.n() - components, result.edges().size());
            assertEquals(result.weight(), result.edges().stream().mapToLong(Graph.Edge::weight).sum());
        }
        assertEquals(2L * graph.m(), prim.edgeScans);
        assertEquals(2 * kruskal.edgeScans + edgeCount, kruskal.dsuOps);
    }
}
