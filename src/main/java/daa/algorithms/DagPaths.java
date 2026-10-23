package daa.algorithms;

import daa.graph.Graph;
import daa.metrics.Metrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DagPaths {
    private DagPaths() { }

    public static final class Result {
        private final long[] distance;
        private final int[] parent;
        private Result(long[] distance, int[] parent) {
            this.distance = distance;
            this.parent = parent;
        }
        public long distanceTo(int vertex) { return distance[vertex]; }
        public long[] distances() { return distance.clone(); }
        public int parentOf(int vertex) { return parent[vertex]; }
        public long sum() {
            long total = 0;
            for (long value : distance) total = Math.addExact(total, value);
            return total;
        }
        public List<Integer> pathTo(int vertex) {
            if (vertex < 0 || vertex >= parent.length) throw new IllegalArgumentException("Invalid vertex");
            List<Integer> path = new ArrayList<>();
            for (int v = vertex; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }

    public static Result shortest(Graph graph, Metrics metrics) {
        return solve(graph, metrics, 1);
    }

    /** Longest paths use negated long weights; Integer.MIN_VALUE is safe. */
    public static Result longest(Graph graph, Metrics metrics) {
        return solve(graph, metrics, -1);
    }

    private static Result solve(Graph graph, Metrics metrics, int sign) {
        // The CSV counts the relaxation phase; Kahn has its own separately counted metric.
        List<Integer> order = TopologicalSort.kahn(graph, new Metrics());
        int[] indegree = graph.inDegrees();
        long[] distance = new long[graph.n()];
        int[] parent = new int[graph.n()];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        for (int v = 0; v < graph.n(); v++) if (indegree[v] == 0) distance[v] = 0;
        for (int v : order) {
            for (Graph.Edge edge : graph.adjacent(v)) {
                metrics.edgeScans++;
                if (distance[v] == Long.MAX_VALUE) continue;
                long candidate = distance[v] + (long) sign * edge.weight();
                metrics.comparisons++;
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    parent[edge.to()] = v;
                }
            }
        }
        for (int v = 0; v < graph.n(); v++) distance[v] *= sign;
        return new Result(distance, parent);
    }
}
