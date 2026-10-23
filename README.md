# DAA Assignment 3 — Graph Algorithms

Java 17+ Maven project. Includes adjacency-list Graph, DSU (union by size + path compression), minimum spanning forest by Kruskal and Prim, iterative Kosaraju SCC, Kahn topological sorting and shortest paths in a DAG (including negative weights).

## Commands

```bash
mvn test
mvn exec:java -Dexec.jvmArgs="-Xmx3g"
```

Alternatively without Maven (benchmark only):

```bash
javac -d out src/main/java/daa/*.java
java -Xmx3g -cp out daa.Benchmark
```

Run these from the project root. The benchmark outputs `results/results.csv`, checking agreement between the MST algorithms and validating topological orders. Each algorithm is timed five times and the median is recorded. `data/` holds disconnected, cyclic and DAG examples.

## Files

- `src/main/java/daa/Graph.java`: graph storage and file parser
- `src/main/java/daa/DSU.java`: union/find
- `src/main/java/daa/Algorithms.java`: graph algorithms
- `src/main/java/daa/Metrics.java`: operation counters
- `src/main/java/daa/Generator.java`: seeded graph generation
- `src/main/java/daa/Benchmark.java`: measurements and CSV
- `src/test/java/daa/AlgorithmsTest.java`: JUnit 5 checks
- `results/plots/`: measurement figures

## Important validation note

The generated results currently **do not match** the assignment's exact reference values. The generator's random-number consumption/order needs to be reconciled with the instructor's implementation before claiming reference parity. MST algorithms agree with each other on the generated graphs. The JUnit tests are included but Maven was not available in the build environment, so `mvn test` has not been independently run here.

## AI acknowledgement

AI assistance was used for creating initial source code, tests, documentation and data visualization. All code and results should be reviewed and understood before academic submission and defense.

## Git workflow to complete locally

Create and push your own repository, feature branches and tag `v1.0`. Repository history and upload to Moodle cannot be fabricated by an archive.
