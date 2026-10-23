package daa;

import java.util.*;

public class Generator {

 public static Graph generate(int n, int m, String type) {
  Random random = new Random(42);

  boolean directed = !type.equals("undirected");
  Graph g = new Graph(n, directed);

  int[] perm = new int[n];

  for (int i = 0; i < n; i++) {
   perm[i] = i;
  }

  for (int i = n - 1; i > 0; i--) {
   int j = random.nextInt(i + 1);

   int temp = perm[i];
   perm[i] = perm[j];
   perm[j] = temp;
  }

  if (type.equals("undirected")) {
   for (int i = 1; i < n; i++) {
    int a = perm[random.nextInt(i)];
    int b = perm[i];
    int w = 1 + random.nextInt(1000);

    g.addEdge(a, b, w);
   }
  }

  int[] rank = new int[n];

  if (type.equals("dag")) {
   for (int i = 0; i < n; i++) {
    rank[perm[i]] = i;
   }
  }

  while (g.edges.size() < m) {
   int a = random.nextInt(n);
   int b = random.nextInt(n);

   if (a == b) {
    continue;
   }

   if (type.equals("dag") && rank[a] > rank[b]) {
    int temp = a;
    a = b;
    b = temp;
   }

   int w = 1 + random.nextInt(1000);
   g.addEdge(a, b, w);
  }

  return g;
 }
}