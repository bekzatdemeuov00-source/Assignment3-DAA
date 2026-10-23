package daa.algorithms;

import daa.graph.Graph;
import daa.graph.Graph.Edge;
import daa.metrics.Metrics;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class MinimumSpanningForest {
    private MinimumSpanningForest() { }

    public record Result(long weight, List<Edge> edges, int components) {
        public Result { edges = List.copyOf(edges); }
    }

    public static Result kruskal(Graph graph, Metrics metrics) {
        graph.requireUndirected();
        Edge[] edges = graph.edges().toArray(Edge[]::new);
        // Own stable merge sort makes comparison counts independent of library sort choices.
        mergeSort(edges, new Edge[edges.length], 0, edges.length, metrics);
        DSU dsu = new DSU(graph.n());
        List<Edge> selected = new ArrayList<>();
        long weight = 0;
        for (Edge edge : edges) {
            metrics.edgeScans++;
            // union itself performs find(u), find(v), and merges only distinct roots.
            if (dsu.union(edge.from(), edge.to())) {
                selected.add(edge);
                weight += edge.weight();
            }
            if (dsu.components() == 1) break;
        }
        metrics.dsuOps += dsu.operations();
        return new Result(weight, selected, dsu.components());
    }

    private static void mergeSort(Edge[] edges, Edge[] buffer, int left, int right, Metrics metrics) {
        if (right - left < 2) return;
        int middle = (left + right) >>> 1;
        mergeSort(edges, buffer, left, middle, metrics);
        mergeSort(edges, buffer, middle, right, metrics);
        int a = left, b = middle, output = left;
        while (a < middle && b < right) {
            metrics.comparisons++;
            buffer[output++] = edges[a].weight() <= edges[b].weight() ? edges[a++] : edges[b++];
        }
        while (a < middle) buffer[output++] = edges[a++];
        while (b < right) buffer[output++] = edges[b++];
        System.arraycopy(buffer, left, edges, left, right - left);
    }

    public static Result prim(Graph graph, Metrics metrics) {
        graph.requireUndirected();
        boolean[] visited = new boolean[graph.n()];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            int result = Integer.compare(a.weight(), b.weight());
            if (result == 0) result = Integer.compare(a.from(), b.from());
            if (result == 0) result = Integer.compare(a.to(), b.to());
            return result;
        });
        List<Edge> selected = new ArrayList<>();
        long weight = 0;
        int components = 0;
        // Vertex 0 first; restarting at the next unvisited vertex handles a forest.
        for (int start = 0; start < graph.n(); start++) {
            if (visited[start]) continue;
            components++;
            queue.add(new Edge(-1, start, 0));
            while (!queue.isEmpty()) {
                Edge current = queue.remove();
                int v = current.to();
                if (visited[v]) continue;
                visited[v] = true;
                if (current.from() != -1) {
                    selected.add(current);
                    weight += current.weight();
                }
                for (Edge edge : graph.adjacent(v)) {
                    metrics.edgeScans++;
                    if (!visited[edge.to()]) queue.add(edge);
                }
            }
        }
        return new Result(weight, selected, components);
    }
}
