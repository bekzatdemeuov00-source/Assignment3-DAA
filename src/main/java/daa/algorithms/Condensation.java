package daa.algorithms;

import daa.graph.Graph;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Condensation {
    private Condensation() { }

    /** Collapses SCCs; parallel transitions keep the largest weight for critical paths. */
    public static Graph build(Graph graph, StrongComponents.Result scc) {
        graph.requireDirected();
        if (graph.n() != scc.size()) throw new IllegalArgumentException("Partition size mismatch");
        Map<Long, Integer> transitions = new LinkedHashMap<>();
        for (Graph.Edge edge : graph.edges()) {
            int from = scc.componentOf(edge.from()), to = scc.componentOf(edge.to());
            if (from == to) continue;
            long key = ((long) from << 32) | (to & 0xffffffffL);
            transitions.merge(key, edge.weight(), Math::max);
        }
        Graph result = new Graph(scc.count(), true);
        for (Map.Entry<Long, Integer> entry : transitions.entrySet())
            result.addEdge((int) (entry.getKey() >>> 32), (int) (long) entry.getKey(), entry.getValue());
        return result;
    }
}
