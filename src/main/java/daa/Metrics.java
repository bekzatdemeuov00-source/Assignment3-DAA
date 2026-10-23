package daa;

public class Metrics {
    public long dsuOps = 0;
    public long edgeScans = 0;
    public long comparisons = 0;
    public long maxDepth = 0;

    public void updateDepth(long depth) {
        maxDepth = Math.max(maxDepth, depth);
    }
}