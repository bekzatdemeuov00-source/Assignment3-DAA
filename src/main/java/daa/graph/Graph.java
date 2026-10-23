package daa.graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** One adjacency-list representation; each logical edge is stored once in edges(). */
public final class Graph {
    public record Edge(int from, int to, int weight) { }

    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges = new ArrayList<>();
    private final int[] indegree;

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("Negative vertex count");
        this.n = n;
        this.directed = directed;
        this.indegree = new int[n];
        adjacency = new ArrayList<>(n);
        for (int v = 0; v < n; v++) adjacency.add(new ArrayList<>());
    }

    public int n() { return n; }
    public int m() { return edges.size(); }
    public boolean directed() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> adjacent(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }
    public int[] inDegrees() { return indegree.clone(); }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        indegree[to]++;
        if (!directed) {
            adjacency.get(to).add(new Edge(to, from, weight));
            indegree[from]++;
        }
    }

    public void requireDirected() {
        if (!directed) throw new IllegalArgumentException("A directed graph is required");
    }

    public void requireUndirected() {
        if (directed) throw new IllegalArgumentException("An undirected graph is required");
    }

    private void checkVertex(int v) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("Vertex out of range: " + v);
    }

    public static Graph readFile(Path path) throws IOException {
        try (Scanner input = new Scanner(path, java.nio.charset.StandardCharsets.UTF_8)) {
            input.useLocale(Locale.ROOT);
            int n = input.nextInt();
            int m = input.nextInt();
            String kind = input.next();
            if (m < 0 || !(kind.equals("directed") || kind.equals("undirected")))
                throw new IllegalArgumentException("Expected: n m directed|undirected");
            Graph graph = new Graph(n, kind.equals("directed"));
            for (int i = 0; i < m; i++)
                graph.addEdge(input.nextInt(), input.nextInt(), input.nextInt());
            if (input.hasNext()) throw new IllegalArgumentException("Data after declared edges");
            return graph;
        } catch (NoSuchElementException | IllegalArgumentException ex) {
            throw new IOException("Invalid graph file " + path + ": " + ex.getMessage(), ex);
        }
    }
}
