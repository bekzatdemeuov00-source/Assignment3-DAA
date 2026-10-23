package daa.algorithms;

/** Union by size and full iterative path compression. */
public final class DSU {
    private int[] parent;
    private int[] size;
    private int components;
    private long findCalls;
    private long successfulUnions;

    public DSU(int n) { makeSet(n); }

    public void makeSet(int n) {
        if (n < 0) throw new IllegalArgumentException("Negative set count");
        parent = new int[n];
        size = new int[n];
        components = n;
        findCalls = 0;
        successfulUnions = 0;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        if (x < 0 || x >= parent.length) throw new IllegalArgumentException("Invalid element: " + x);
        findCalls++;
        int root = x;
        while (root != parent[root]) root = parent[root];
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** Finds both roots; the union counter increases only when two sets merge. */
    public boolean union(int x, int y) {
        int a = find(x);
        int b = find(y);
        if (a == b) return false;
        if (size[a] < size[b]) {
            int temp = a;
            a = b;
            b = temp;
        }
        parent[b] = a;
        size[a] += size[b];
        components--;
        successfulUnions++;
        return true;
    }

    public int components() { return components; }
    public long findCalls() { return findCalls; }
    public long successfulUnions() { return successfulUnions; }
    public long operations() { return findCalls + successfulUnions; }
}
