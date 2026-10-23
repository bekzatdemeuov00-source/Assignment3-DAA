package daa;

import daa.algorithms.*;
import daa.graph.Graph;
import daa.metrics.Metrics;
import java.nio.file.Path;
import java.util.Arrays;

public final class Demo {
    private Demo() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Usage: daa.Demo data/dag.txt");
        Graph graph = Graph.readFile(Path.of(args[0]));
        System.out.printf("vertices=%d edges=%d directed=%s%n", graph.n(), graph.m(), graph.directed());
        if (!graph.directed()) {
            var kruskal = MinimumSpanningForest.kruskal(graph, new Metrics());
            var prim = MinimumSpanningForest.prim(graph, new Metrics());
            System.out.printf("Kruskal weight=%d, Prim weight=%d, components=%d%n",
                    kruskal.weight(), prim.weight(), kruskal.components());
            kruskal.edges().forEach(System.out::println);
        } else {
            var scc = StrongComponents.kosaraju(graph, new Metrics());
            System.out.println("SCC count=" + scc.count() + "; membership=" + Arrays.toString(scc.components()));
            try {
                System.out.println("Topological order: " + TopologicalSort.kahn(graph, new Metrics()));
                var paths = DagPaths.shortest(graph, new Metrics());
                for (int v = 0; v < graph.n(); v++)
                    System.out.printf("vertex=%d distance=%d path=%s%n", v, paths.distanceTo(v), paths.pathTo(v));
            } catch (IllegalArgumentException cycle) {
                System.out.println(cycle.getMessage());
            }
            Graph condensed = Condensation.build(graph, scc);
            var critical = DagPaths.longest(condensed, new Metrics());
            int end = -1;
            for (int v = 0; v < condensed.n(); v++)
                if (end == -1 || critical.distanceTo(v) > critical.distanceTo(end)) end = v;
            System.out.println("Condensation: " + condensed.n() + " vertices, " + condensed.m() + " edges");
            if (end != -1) System.out.println("Critical component path: " + critical.pathTo(end)
                    + "; transition weight=" + critical.distanceTo(end));
        }
    }
}
