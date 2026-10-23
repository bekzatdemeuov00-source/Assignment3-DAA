package daa;

public class DSU {
    private int[] parent;
    private int[] size;
    private int count;

    public long findCalls = 0;
    public long unionCalls = 0;

    public DSU(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        parent = new int[n];
        size = new int[n];
        count = n;
        findCalls = 0;
        unionCalls = 0;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        findCalls++;

        int root = x;

        while (root != parent[root]) {
            root = parent[root];
        }

        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }

        return root;
    }

    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) {
            return false;
        }

        if (size[rootA] < size[rootB]) {
            int temp = rootA;
            rootA = rootB;
            rootB = temp;
        }

        parent[rootB] = rootA;
        size[rootA] += size[rootB];

        count--;
        unionCalls++;
        return true;
    }

    public int components() {
        return count;
    }
}