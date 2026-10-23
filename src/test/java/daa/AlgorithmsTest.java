package daa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class AlgorithmsTest {

 @Test
 void testDSU() {
  DSU dsu = new DSU(5);

  dsu.union(0, 1);
  dsu.union(1, 2);

  assertEquals(dsu.find(0), dsu.find(2));
  assertEquals(3, dsu.components());

  assertFalse(dsu.union(0, 2));
 }

 @Test
 void testMST() {
  Graph g = new Graph(4, false);

  g.addEdge(0, 1, 1);
  g.addEdge(1, 2, 2);
  g.addEdge(2, 3, 3);
  g.addEdge(0, 3, 10);

  Algorithms.MSTResult k =
          Algorithms.kruskal(g, new Metrics());

  Algorithms.MSTResult p =
          Algorithms.prim(g, new Metrics());

  assertEquals(6, k.weight);
  assertEquals(k.weight, p.weight);
  assertEquals(3, k.edges);
 }

 @Test
 void testDisconnectedGraph() {
  Graph g = new Graph(5, false);

  g.addEdge(0, 1, 2);
  g.addEdge(2, 3, 4);

  Algorithms.MSTResult k =
          Algorithms.kruskal(g, new Metrics());

  Algorithms.MSTResult p =
          Algorithms.prim(g, new Metrics());

  assertEquals(6, k.weight);
  assertEquals(k.weight, p.weight);
  assertEquals(3, k.components);
 }

 @Test
 void testKosaraju() {
  Graph g = new Graph(5, true);

  g.addEdge(0, 1, 1);
  g.addEdge(1, 2, 1);
  g.addEdge(2, 0, 1);
  g.addEdge(2, 3, 1);
  g.addEdge(3, 4, 1);

  assertEquals(3,
          Algorithms.kosaraju(g, new Metrics()));
 }

 @Test
 void testTopologicalSort() {
  Graph g = new Graph(4, true);

  g.addEdge(0, 1, 1);
  g.addEdge(0, 2, 1);
  g.addEdge(1, 3, 1);
  g.addEdge(2, 3, 1);

  List<Integer> order =
          Algorithms.kahn(g, new Metrics());

  assertTrue(Algorithms.validOrder(g, order));
 }

 @Test
 void testCycle() {
  Graph g = new Graph(3, true);

  g.addEdge(0, 1, 1);
  g.addEdge(1, 2, 1);
  g.addEdge(2, 0, 1);

  assertThrows(IllegalArgumentException.class,
          () -> Algorithms.kahn(g, new Metrics()));
 }

 @Test
 void testShortestPath() {
  Graph g = new Graph(4, true);

  g.addEdge(0, 1, 5);
  g.addEdge(0, 2, 2);
  g.addEdge(2, 1, -4);
  g.addEdge(1, 3, 3);

  Algorithms.PathResult result =
          Algorithms.dagShortestPath(g, new Metrics());

  assertEquals(-2, result.dist[1]);
  assertEquals(1, result.dist[3]);

  assertEquals(
          Arrays.asList(0, 2, 1, 3),
          result.getPath(3)
  );
 }

 @Test
 void testLargeGraph() {
  Graph g = new Graph(100000, true);

  for (int i = 0; i < 99999; i++) {
   g.addEdge(i, i + 1, 1);
  }

  assertEquals(100000,
          Algorithms.kosaraju(g, new Metrics()));
 }

 @Test
 void testRandomMST() {
  Random random = new Random(42);

  for (int t = 0; t < 100; t++) {
   Graph g = new Graph(20, false);

   for (int i = 1; i < 20; i++) {
    g.addEdge(
            i - 1, i, random.nextInt(100) + 1
    );
   }

   for (int j = 0; j < 50; j++) {
    int a = random.nextInt(20);
    int b = random.nextInt(20);

    if (a != b) {
     g.addEdge(a, b, random.nextInt(100) + 1);
    }
   }

   Algorithms.MSTResult k =
           Algorithms.kruskal(g, new Metrics());

   Algorithms.MSTResult p =
           Algorithms.prim(g, new Metrics());

   assertEquals(k.weight, p.weight);
   assertEquals(19, k.edges);
   assertEquals(19, p.edges);
  }
 }
}