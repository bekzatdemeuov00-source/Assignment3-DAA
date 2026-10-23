package daa.algorithms;

import daa.graph.Graph;
import daa.graph.Graph.Edge;
import daa.metrics.Metrics;
import java.util.Arrays;

public final class StrongComponents {
    private StrongComponents() { }

    public static final class Result {
        private final int count;
        private final int[] component;
        private Result(int count, int[] component) {
            this.count = count;
            this.component = component;
        }
        public int count() { return count; }
        public int size() { return component.length; }
        public int componentOf(int vertex) { return component[vertex]; }
        public int[] components() { return component.clone(); }
    }

    public static Result kosaraju(Graph graph, Metrics metrics) {
        graph.requireDirected();
        int n = graph.n(), finished = 0;
        int[] order = new int[n], stack = new int[n], next = new int[n];
        boolean[] visited = new boolean[n];
        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            int depth = 1;
            stack[0] = start;
            next[0] = 0;
            visited[start] = true;
            metrics.observeDepth(depth);
            while (depth > 0) {
                int frame = depth - 1, v = stack[frame];
                if (next[frame] < graph.adjacent(v).size()) {
                    Edge edge = graph.adjacent(v).get(next[frame]++);
                    metrics.edgeScans++;
                    if (!visited[edge.to()]) {
                        visited[edge.to()] = true;
                        stack[depth] = edge.to();
                        next[depth++] = 0;
                        metrics.observeDepth(depth);
                    }
                } else {
                    order[finished++] = v;
                    depth--;
                }
            }
        }

        // Representation preparation is excluded from DFS edgeScans (assignment: exactly 2m).
        int[] degrees = graph.inDegrees();
        int[][] reversed = new int[n][];
        for (int v = 0; v < n; v++) reversed[v] = new int[degrees[v]];
        Arrays.fill(degrees, 0);
        for (Edge edge : graph.edges()) reversed[edge.to()][degrees[edge.to()]++] = edge.from();
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int count = 0;
        for (int i = finished - 1; i >= 0; i--) {
            int start = order[i];
            if (component[start] != -1) continue;
            int depth = 1;
            stack[0] = start;
            next[0] = 0;
            component[start] = count;
            metrics.observeDepth(depth);
            while (depth > 0) {
                int frame = depth - 1, v = stack[frame];
                if (next[frame] < reversed[v].length) {
                    int to = reversed[v][next[frame]++];
                    metrics.edgeScans++;
                    if (component[to] == -1) {
                        component[to] = count;
                        stack[depth] = to;
                        next[depth++] = 0;
                        metrics.observeDepth(depth);
                    }
                } else {
                    depth--;
                }
            }
            count++;
        }
        return new Result(count, component);
    }

    /** Bonus A: iterative Tarjan, with a DFS frame stack and a separate active stack. */
    public static Result tarjan(Graph graph, Metrics metrics) {
        graph.requireDirected();
        int n = graph.n(), clock = 0, count = 0, activeSize = 0;
        int[] index = new int[n], low = new int[n], component = new int[n];
        int[] stack = new int[n], next = new int[n], active = new int[n], parent = new int[n];
        boolean[] onStack = new boolean[n];
        Arrays.fill(index, -1);
        Arrays.fill(component, -1);
        Arrays.fill(parent, -1);
        for (int start = 0; start < n; start++) {
            if (index[start] != -1) continue;
            int depth = 1;
            stack[0] = start;
            next[0] = 0;
            index[start] = low[start] = clock++;
            active[activeSize++] = start;
            onStack[start] = true;
            metrics.observeDepth(Math.max(depth, activeSize));
            while (depth > 0) {
                int frame = depth - 1, v = stack[frame];
                if (next[frame] < graph.adjacent(v).size()) {
                    int to = graph.adjacent(v).get(next[frame]++).to();
                    metrics.edgeScans++;
                    if (index[to] == -1) {
                        parent[to] = v;
                        index[to] = low[to] = clock++;
                        active[activeSize++] = to;
                        onStack[to] = true;
                        stack[depth] = to;
                        next[depth++] = 0;
                        metrics.observeDepth(Math.max(depth, activeSize));
                    } else if (onStack[to]) {
                        low[v] = Math.min(low[v], index[to]);
                    }
                } else {
                    depth--;
                    if (parent[v] != -1) low[parent[v]] = Math.min(low[parent[v]], low[v]);
                    if (low[v] == index[v]) {
                        int to;
                        do {
                            to = active[--activeSize];
                            onStack[to] = false;
                            component[to] = count;
                        } while (to != v);
                        count++;
                    }
                }
            }
        }
        return new Result(count, component);
    }

    /** IDs may differ: compare partitions using a bijection in O(V). */
    public static boolean samePartition(Result first, Result second) {
        if (first.size() != second.size() || first.count() != second.count()) return false;
        int[] forward = new int[first.count()], reverse = new int[second.count()];
        Arrays.fill(forward, -1);
        Arrays.fill(reverse, -1);
        for (int v = 0; v < first.size(); v++) {
            int a = first.componentOf(v), b = second.componentOf(v);
            if ((forward[a] != -1 && forward[a] != b) || (reverse[b] != -1 && reverse[b] != a))
                return false;
            forward[a] = b;
            reverse[b] = a;
        }
        return true;
    }
}
