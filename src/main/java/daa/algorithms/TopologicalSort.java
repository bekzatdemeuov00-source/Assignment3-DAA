package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class TopologicalSort {
    private TopologicalSort() { }

    public static List<Integer> kahn(Graph graph, Metrics metrics) {
        graph.requireDirected();
        int[] indegree = graph.inDegrees();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < graph.n(); v++) if (indegree[v] == 0) queue.add(v);
        List<Integer> order = new ArrayList<>(graph.n());
        while (!queue.isEmpty()) {
            int v = queue.remove();
            order.add(v);
            for (Graph.Edge edge : graph.adjacent(v)) {
                metrics.edgeScans++;
                if (--indegree[edge.to()] == 0) queue.add(edge.to());
            }
        }
        if (order.size() != graph.n())
            throw new IllegalArgumentException("Graph contains a directed cycle; no topological order");
        return List.copyOf(order);
    }

    public static boolean isValid(Graph graph, List<Integer> order) {
        if (!graph.directed() || order.size() != graph.n()) return false;
        int[] position = new int[graph.n()];
        boolean[] seen = new boolean[graph.n()];
        for (int i = 0; i < order.size(); i++) {
            Integer vertex = order.get(i);
            if (vertex == null || vertex < 0 || vertex >= graph.n() || seen[vertex]) return false;
            seen[vertex] = true;
            position[vertex] = i;
        }
        for (Graph.Edge edge : graph.edges())
            if (position[edge.from()] >= position[edge.to()]) return false;
        return true;
    }
}
