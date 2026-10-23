package daa.metrics;

/** Algorithm-local counts, reset by constructing a new instance for each run. */
public final class Metrics {
    public long dsuOps;
    public long edgeScans;
    public long comparisons;
    public int maxDepth;

    public void observeDepth(int depth) { maxDepth = Math.max(maxDepth, depth); }

    public boolean sameAs(Metrics other) {
        return dsuOps == other.dsuOps && edgeScans == other.edgeScans
                && comparisons == other.comparisons && maxDepth == other.maxDepth;
    }

    public long operations() { return dsuOps + edgeScans + comparisons; }
}
