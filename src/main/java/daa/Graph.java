package daa;

import java.util.*;
import java.io.*;

public class Graph {
    public int n;
    public boolean directed;
    public List<Edge>[] adj;
    public List<Edge> edges = new ArrayList<>();

    public static class Edge {
        public int from, to, weight;

        public Edge(int from, int to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    @SuppressWarnings("unchecked")
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        adj = new ArrayList[n];

        for (int i = 0; i < n; i++) {
            adj[i] = new ArrayList<>();
        }
    }

    public void addEdge(int u, int v, int w) {
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj[u].add(e);

        if (!directed) {
            adj[v].add(new Edge(v, u, w));
        }
    }

    public static Graph readFile(String filename) throws IOException {
        try (Scanner sc = new Scanner(new File(filename))) {
            int n = sc.nextInt();
            int m = sc.nextInt();
            boolean directed = sc.next().equalsIgnoreCase("directed");

            Graph g = new Graph(n, directed);

            for (int i = 0; i < m; i++) {
                g.addEdge(sc.nextInt(), sc.nextInt(), sc.nextInt());
            }

            return g;
        }
    }
}