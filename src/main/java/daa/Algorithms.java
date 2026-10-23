package daa;

import java.util.*;

public class Algorithms {

    public static class MSTResult {
        public long weight;
        public int edges;
        public int components;

        public MSTResult(long weight, int edges, int components) {
            this.weight = weight;
            this.edges = edges;
            this.components = components;
        }
    }

    public static MSTResult kruskal(Graph g, Metrics m) {
        if (g.directed) {
            throw new IllegalArgumentException("Graph must be undirected");
        }

        List<Graph.Edge> edges = new ArrayList<>(g.edges);

        edges.sort((a, b) -> {
            m.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });

        DSU dsu = new DSU(g.n);
        long total = 0;
        int used = 0;

        for (Graph.Edge e : edges) {
            m.edgeScans++;

            if (dsu.union(e.from, e.to)) {
                total += e.weight;
                used++;
            }

            if (used == g.n - 1) {
                break;
            }
        }

        m.dsuOps = dsu.findCalls + dsu.unionCalls;

        return new MSTResult(total, used, dsu.components());
    }

    public static MSTResult prim(Graph g, Metrics m) {
        if (g.directed) {
            throw new IllegalArgumentException("Graph must be undirected");
        }

        boolean[] visited = new boolean[g.n];

        PriorityQueue<Graph.Edge> pq = new PriorityQueue<>(
                (a, b) -> {
                    m.comparisons++;
                    return Integer.compare(a.weight, b.weight);
                }
        );

        long total = 0;
        int used = 0;
        int components = 0;

        for (int start = 0; start < g.n; start++) {
            if (visited[start]) {
                continue;
            }

            components++;
            pq.add(new Graph.Edge(-1, start, 0));

            while (!pq.isEmpty()) {
                Graph.Edge current = pq.poll();
                int v = current.to;

                if (visited[v]) {
                    continue;
                }

                visited[v] = true;

                if (current.from != -1) {
                    total += current.weight;
                    used++;
                }

                for (Graph.Edge e : g.adj[v]) {
                    m.edgeScans++;

                    if (!visited[e.to]) {
                        pq.add(e);
                    }
                }
            }
        }

        return new MSTResult(total, used, components);
    }

    public static int kosaraju(Graph g, Metrics m) {
        int n = g.n;
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>();

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            Stack<int[]> stack = new Stack<>();
            stack.push(new int[]{start, 0});
            visited[start] = true;

            while (!stack.isEmpty()) {
                m.updateDepth(stack.size());

                int[] top = stack.peek();
                int v = top[0];

                if (top[1] < g.adj[v].size()) {
                    Graph.Edge e = g.adj[v].get(top[1]++);
                    m.edgeScans++;

                    if (!visited[e.to]) {
                        visited[e.to] = true;
                        stack.push(new int[]{e.to, 0});
                    }
                } else {
                    order.add(v);
                    stack.pop();
                }
            }
        }

        List<Integer>[] reversed = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            reversed[i] = new ArrayList<>();
        }

        for (Graph.Edge e : g.edges) {
            reversed[e.to].add(e.from);
        }

        Arrays.fill(visited, false);
        int components = 0;

        for (int i = order.size() - 1; i >= 0; i--) {
            int start = order.get(i);

            if (visited[start]) {
                continue;
            }

            components++;
            Stack<Integer> stack = new Stack<>();
            stack.push(start);
            visited[start] = true;

            while (!stack.isEmpty()) {
                m.updateDepth(stack.size());

                int v = stack.pop();

                for (int to : reversed[v]) {
                    m.edgeScans++;

                    if (!visited[to]) {
                        visited[to] = true;
                        stack.push(to);
                    }
                }
            }
        }

        return components;
    }

    public static List<Integer> kahn(Graph g, Metrics m) {
        int[] indegree = new int[g.n];

        for (Graph.Edge e : g.edges) {
            indegree[e.to]++;
        }

        Queue<Integer> queue = new ArrayDeque<>();

        for (int i = 0; i < g.n; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> order = new ArrayList<>();

        while (!queue.isEmpty()) {
            int v = queue.poll();
            order.add(v);

            for (Graph.Edge e : g.adj[v]) {
                m.edgeScans++;
                indegree[e.to]--;

                if (indegree[e.to] == 0) {
                    queue.add(e.to);
                }
            }
        }

        if (order.size() != g.n) {
            throw new IllegalArgumentException("Graph contains a cycle");
        }

        return order;
    }

    public static class PathResult {
        public long[] dist;
        public int[] parent;

        public PathResult(long[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }

        public List<Integer> getPath(int v) {
            List<Integer> path = new ArrayList<>();

            if (dist[v] == Long.MAX_VALUE) {
                return path;
            }

            while (v != -1) {
                path.add(v);
                v = parent[v];
            }

            Collections.reverse(path);
            return path;
        }
    }

    public static PathResult dagShortestPath(Graph g, Metrics m) {
        List<Integer> order = kahn(g, new Metrics());

        int[] indegree = new int[g.n];

        for (Graph.Edge e : g.edges) {
            indegree[e.to]++;
        }

        long[] dist = new long[g.n];
        int[] parent = new int[g.n];

        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(parent, -1);

        for (int i = 0; i < g.n; i++) {
            if (indegree[i] == 0) {
                dist[i] = 0;
            }
        }

        for (int v : order) {
            for (Graph.Edge e : g.adj[v]) {
                m.edgeScans++;

                if (dist[v] == Long.MAX_VALUE) {
                    continue;
                }

                long newDist = dist[v] + e.weight;

                m.comparisons++;

                if (newDist < dist[e.to]) {
                    dist[e.to] = newDist;
                    parent[e.to] = v;
                }
            }
        }

        return new PathResult(dist, parent);
    }

    public static boolean validOrder(Graph g, List<Integer> order) {
        if (order.size() != g.n) {
            return false;
        }

        int[] pos = new int[g.n];
        boolean[] seen = new boolean[g.n];

        for (int i = 0; i < order.size(); i++) {
            int v = order.get(i);

            if (v < 0 || v >= g.n || seen[v]) {
                return false;
            }

            seen[v] = true;
            pos[v] = i;
        }

        for (Graph.Edge e : g.edges) {
            if (pos[e.from] >= pos[e.to]) {
                return false;
            }
        }

        return true;
    }
}