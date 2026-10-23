package daa;

import daa.algorithms.*;
import daa.benchmark.Generator;
import daa.benchmark.ReferenceValues;
import daa.graph.Graph;
import daa.metrics.Metrics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {
    @Test void firstFourReferenceCasesAndDeterminism() {
        for (var e : ReferenceValues.TABLE.subList(0,4)) {
            Graph u = Generator.generate(e.n(),e.m(),Generator.Kind.UNDIRECTED);
            assertEquals(e.mst(),MinimumSpanningForest.kruskal(u,new Metrics()).weight());
            assertEquals(e.mst(),MinimumSpanningForest.prim(u,new Metrics()).weight());
            Graph d = Generator.generate(e.n(),e.m(),Generator.Kind.DIRECTED);
            assertEquals(e.scc(),StrongComponents.kosaraju(d,new Metrics()).count());
            Graph dag = Generator.generate(e.n(),e.m(),Generator.Kind.DAG);
            assertEquals(e.distanceSum(),DagPaths.shortest(dag,new Metrics()).sum());
        }
        for (var kind : Generator.Kind.values()) {
            Graph a = Generator.generate(25,100,kind), b = Generator.generate(25,100,kind);
            assertEquals(a.edges(),b.edges());
            assertEquals(100,a.m());
            for (Graph.Edge edge : a.edges()) {
                assertNotEquals(edge.from(),edge.to());
                assertTrue(edge.weight() >= 1 && edge.weight() <= 1000);
            }
        }
    }
    @Test void invalidSizesCannotLoopForever() {
        assertThrows(IllegalArgumentException.class,()->Generator.generate(1,1,Generator.Kind.DIRECTED));
        assertThrows(IllegalArgumentException.class,()->Generator.generate(5,3,Generator.Kind.UNDIRECTED));
        assertThrows(IllegalArgumentException.class,()->Generator.generate(-1,0,Generator.Kind.DAG));
        assertEquals(0,Generator.generate(0,0,Generator.Kind.DAG).n());
    }
}
