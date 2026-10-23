package daa;

import daa.algorithms.DSU;
import daa.graph.Graph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GraphDsuTest {
    @Test void randomUnionsMatchReferencePartition() {
        Random random = new Random(812);
        for (int n : new int[]{1, 2, 37, 200}) {
            DSU dsu = new DSU(n);
            int[] labels = new int[n];
            for (int i = 0; i < n; i++) labels[i] = i;
            int count = n;
            long successes = 0;
            for (int step = 0; step < 1000; step++) {
                int a = random.nextInt(n), b = random.nextInt(n);
                boolean merged = labels[a] != labels[b];
                assertEquals(merged, dsu.union(a, b));
                if (merged) {
                    int old = labels[b], replacement = labels[a];
                    for (int i = 0; i < n; i++) if (labels[i] == old) labels[i] = replacement;
                    count--;
                    successes++;
                }
                for (int v = 0; v < n; v++)
                    assertEquals(labels[v] == labels[a], dsu.find(v) == dsu.find(a));
                assertEquals(count, dsu.components());
                assertEquals(Arrays.stream(labels).distinct().count(), dsu.components());
                assertEquals(successes, dsu.successfulUnions());
            }
            dsu.makeSet(n);
            assertEquals(n, dsu.components());
            assertEquals(0, dsu.operations());
        }
    }

    @Test void countsActualFindsAndSuccessfulUnions() {
        DSU dsu = new DSU(3);
        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(1, 0));
        dsu.find(2);
        assertEquals(5, dsu.findCalls());
        assertEquals(1, dsu.successfulUnions());
        assertEquals(6, dsu.operations());
        assertEquals(0, new DSU(0).components());
        assertThrows(IllegalArgumentException.class, () -> dsu.find(3));
        assertThrows(IllegalArgumentException.class, () -> new DSU(-1));
    }

    @Test void supportsLoopsParallelEdgesAndIsolates() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, -1);
        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 2);
        assertEquals(3, graph.m());
        assertEquals(4, graph.adjacent(0).size());
        assertEquals(2, graph.adjacent(1).size());
        assertTrue(graph.adjacent(2).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> graph.edges().clear());
        int[] degrees = graph.inDegrees();
        degrees[0] = 100;
        assertEquals(4, graph.inDegrees()[0]);
        assertThrows(IllegalArgumentException.class, () -> graph.addEdge(3, 0, 1));
    }

    @Test void parsesAllSuppliedExamples() throws IOException {
        assertEquals(2, Graph.readFile(Path.of("data/disconnected.txt")).m());
        assertEquals(4, Graph.readFile(Path.of("data/cyclic.txt")).m());
        assertEquals(5, Graph.readFile(Path.of("data/dag.txt")).m());
    }

    @Test void rejectsMalformedFiles(@TempDir Path temp) throws IOException {
        Path file = temp.resolve("bad.txt");
        for (String input : new String[]{"2 1 unknown\n0 1 1", "2 1 directed\n0 2 1",
                "2 2 directed\n0 1 1", "2 -1 directed", "2 0 directed\n0 1 2", "-1 0 directed"}) {
            Files.writeString(file, input);
            assertThrows(IOException.class, () -> Graph.readFile(file), input);
        }
    }
}
