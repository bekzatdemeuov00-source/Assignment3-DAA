package daa;

import java.io.*;
import java.util.*;

public class Benchmark {

 static int[] sizes = {1000, 3162, 10000, 31623, 100000};

 interface Task {
  long run(Metrics metrics);
 }

 static void measure(PrintWriter writer, String name,
                     String graphType, Graph g, Task task) {

  long[] times = new long[5];
  long result = 0;
  Metrics last = null;

  for (int i = 0; i < 5; i++) {
   Metrics metrics = new Metrics();

   long start = System.nanoTime();
   long value = task.run(metrics);
   long end = System.nanoTime();

   times[i] = end - start;

   if (i > 0 && value != result) {
    throw new RuntimeException("Different results!");
   }

   result = value;
   last = metrics;
  }

  Arrays.sort(times);

  double median = times[2] / 1_000_000.0;

  writer.printf(
          Locale.US,
          "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d%n",
          name, graphType, g.n, g.edges.size(),
          median, last.dsuOps, last.edgeScans,
          last.comparisons, last.maxDepth, result
  );

  System.out.println(name + " | n=" + g.n
          + " | result=" + result);
 }

 public static void main(String[] args) throws Exception {
  new File("results").mkdirs();

  try (PrintWriter writer = new PrintWriter(
          new FileWriter("results/results.csv"))) {

   writer.println(
           "algorithm,graph,n,m,time_ms,dsu_ops,"
                   + "edge_scans,comparisons,max_depth,result"
   );

   for (int n : sizes) {
    for (int density : new int[]{4, 20}) {
     int m = n * density;

     System.out.println("\nGraph: " + n + " / " + m);

     Graph undirected =
             Generator.generate(n, m, "undirected");

     Graph directed =
             Generator.generate(n, m, "directed");

     Graph dag =
             Generator.generate(n, m, "dag");

     long k = Algorithms.kruskal(
             undirected, new Metrics()).weight;

     long p = Algorithms.prim(
             undirected, new Metrics()).weight;

     if (k != p) {
      throw new RuntimeException(
              "MST mismatch: " + k + " vs " + p);
     }

     measure(writer, "Kruskal", "undirected",
             undirected,
             x -> Algorithms.kruskal(
                     undirected, x).weight);

     measure(writer, "Prim", "undirected",
             undirected,
             x -> Algorithms.prim(
                     undirected, x).weight);

     measure(writer, "Kosaraju", "directed",
             directed,
             x -> Algorithms.kosaraju(directed, x));

     measure(writer, "Kahn", "dag",
             dag, x -> {
              List<Integer> order =
                      Algorithms.kahn(dag, x);

              if (!Algorithms.validOrder(dag, order)) {
               throw new RuntimeException(
                       "Invalid topological order");
              }

              return order.size();
             });

     measure(writer, "DagShortestPath", "dag",
             dag, x -> {
              Algorithms.PathResult paths =
                      Algorithms.dagShortestPath(dag, x);

              long sum = 0;

              for (long d : paths.dist) {
               if (d != Long.MAX_VALUE) {
                sum += d;
               }
              }

              return sum;
             });
    }
   }
  }

  System.out.println("\nFinished! Check results/results.csv");
 }
}