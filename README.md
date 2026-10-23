# DAA Assignment 3 — Graph Algorithms

**Bekzat Demeuov | SE-2509**  
Repository: https://github.com/bekzatdemeuov00-source/Assignment3-DAA  
Java 17+ Maven project. Includes adjacency-list Graph, counted DSU (union by size + path compression), Kruskal and Prim minimum spanning forests, iterative Kosaraju and Tarjan SCC, Kahn topological sorting, DAG shortest paths (including negative weights), and SCC condensation with longest critical paths.

## Commands

Run everything from the project root with Python 3.10+, Java 17+, and matplotlib:

```powershell
python -m pip install -r scripts/requirements.txt
python scripts/reproduce.py
```

This one command builds the project, runs all JUnit tests, checks all ten reference triples, writes 50 benchmark rows and 10 Tarjan rows, and regenerates the ratio/slope analysis and three plots. Maven is downloaded automatically by the checked-in wrapper. To run just tests: `./mvnw test` (Windows: `.\mvnw.cmd test`). To run the self-checking full benchmark after tests: `./mvnw -Pbenchmark verify` (Windows: `.\mvnw.cmd -Pbenchmark verify`). To inspect a sample: `./mvnw -q -DskipTests package`, then `java -cp target/classes daa.Demo data/dag.txt`.

## Files

- `src/main/java/daa/graph/Graph.java`, `algorithms/`, `metrics/`, `benchmark/`: graph, algorithms, counters and reproducible benchmark
- `src/test/java/daa/`: 19 JUnit 5 tests, including brute force and stack safety
- `results/results.csv`: 50 core measurements; `results/bonus_results.csv`: 10 Tarjan measurements
- `results/normalized.csv`, `results/slopes.csv`: counter formulas and fitted slopes
- `results/plots/`: the three required figures; `results/environment.txt`: measured runtime environment
- `REPORT.md`: five-section report with proofs, complexity, tables and plots
- `data/`: four small examples, including disconnected and cyclic graphs

## AI acknowledgement

AI assistance was used to help implement and document this assignment and to prepare its plots. Bekzat Demeuov is responsible for reviewing and understanding the submitted code, results, proofs, and complexity explanations before the defense.

## Git branches and release

Branches: `main`, `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths`, `feature/metrics`. Release tag: `v1.0`. To publish, run `git push -u origin main feature/dsu feature/mst feature/scc feature/paths feature/metrics`, then `git push origin v1.0`.
