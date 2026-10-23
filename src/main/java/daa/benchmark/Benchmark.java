package daa.benchmark;

import daa.algorithms.*;
import daa.graph.Graph;
import daa.metrics.Metrics;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import static daa.benchmark.ReferenceValues.require;

public final class Benchmark {
    private Benchmark() { }
    private static final int RUNS = 5;
    private static final String HEADER =
        "algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result";
    private record Measurement<T>(String csv, T result) { }

    private static <T> Measurement<T> measure(String name, String kind, Graph graph,
            Function<Metrics,T> run, ToLongFunction<T> value, Consumer<T> validate) {
        long[] times = new long[RUNS];
        Metrics first = null;
        long firstValue = 0;
        T last = null;
        for (int i = 0; i < RUNS; i++) {
            Metrics metrics = new Metrics();
            long start = System.nanoTime();
            T result = run.apply(metrics);
            times[i] = System.nanoTime() - start;
            validate.accept(result); // Validation is outside the timed region.
            long scalar = value.applyAsLong(result);
            if (i == 0) {
                first = metrics;
                firstValue = scalar;
            } else {
                require(firstValue == scalar, name + ": inconsistent result");
                require(first.sameAs(metrics), name + ": inconsistent counters");
            }
            long scans = switch (name) {
                case "Prim", "Kosaraju" -> 2L * graph.m();
                case "Kahn", "DagShortestPath", "Tarjan" -> graph.m();
                default -> -1;
            };
            if (scans >= 0) require(metrics.edgeScans == scans, name + ": wrong scan count");
            last = result;
        }
        Arrays.sort(times);
        String row = String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
            name, kind, graph.n(), graph.m(), times[RUNS/2] / 1_000_000.0,
            first.dsuOps, first.edgeScans, first.comparisons, first.maxDepth, firstValue);
        return new Measurement<>(row, last);
    }

    private static void mst(ReferenceValues.Expected e, List<String> rows) {
        Graph g = Generator.generate(e.n(), e.m(), Generator.Kind.UNDIRECTED);
        Consumer<MinimumSpanningForest.Result> validate = r -> {
            require(r.weight() == e.mst(), "MST reference mismatch: " + e);
            require(r.edges().size() == g.n()-1 && r.components() == 1, "Invalid spanning tree");
        };
        var k = measure("Kruskal", "undirected", g,
            m -> MinimumSpanningForest.kruskal(g,m), MinimumSpanningForest.Result::weight, validate);
        var p = measure("Prim", "undirected", g,
            m -> MinimumSpanningForest.prim(g,m), MinimumSpanningForest.Result::weight, validate);
        require(k.result.weight() == p.result.weight(), "Kruskal/Prim disagreement");
        rows.add(k.csv);
        rows.add(p.csv);
    }

    private static void scc(ReferenceValues.Expected e, List<String> rows, List<String> bonus) {
        Graph g = Generator.generate(e.n(), e.m(), Generator.Kind.DIRECTED);
        var k = measure("Kosaraju", "directed", g, m -> StrongComponents.kosaraju(g,m),
            StrongComponents.Result::count,
            r -> require(r.count() == e.scc(), "SCC reference mismatch: " + e));
        var t = measure("Tarjan", "directed", g, m -> StrongComponents.tarjan(g,m),
            StrongComponents.Result::count,
            r -> require(StrongComponents.samePartition(k.result,r), "Tarjan partition mismatch"));
        rows.add(k.csv);
        bonus.add(t.csv);
    }

    private static void dag(ReferenceValues.Expected e, List<String> rows) {
        Graph g = Generator.generate(e.n(), e.m(), Generator.Kind.DAG);
        var k = measure("Kahn", "dag", g, m -> TopologicalSort.kahn(g,m), List::size,
            r -> require(TopologicalSort.isValid(g,r), "Invalid topological order"));
        var p = measure("DagShortestPath", "dag", g, m -> DagPaths.shortest(g,m),
            DagPaths.Result::sum,
            r -> require(r.sum() == e.distanceSum(), "DAG reference mismatch: " + e));
        rows.add(k.csv);
        rows.add(p.csv);
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 0) throw new IllegalArgumentException("Benchmark takes no arguments");
        List<String> rows = new ArrayList<>(), bonus = new ArrayList<>();
        rows.add(HEADER); bonus.add(HEADER);
        System.out.println("n,m,mst_weight,scc_count,dag_distance_sum");
        for (var expected : ReferenceValues.TABLE) {
            mst(expected,rows);
            scc(expected,rows,bonus);
            dag(expected,rows);
            System.out.printf("%d,%d,%d,%d,%d%n", expected.n(), expected.m(),
                expected.mst(), expected.scc(), expected.distanceSum());
        }
        require(rows.size() == 51 && bonus.size() == 11, "Incorrect CSV row count");
        Files.createDirectories(Path.of("results"));
        write(Path.of("results/results.csv"),rows);
        write(Path.of("results/bonus_results.csv"),bonus);
        write(Path.of("results/environment.txt"),List.of(
            "Java: " + System.getProperty("java.version"),
            "VM: " + System.getProperty("java.vm.name"),
            "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch"),
            "Available processors: " + Runtime.getRuntime().availableProcessors(),
            "Max JVM heap bytes: " + Runtime.getRuntime().maxMemory(),
            "Seed: 42; runs: 5; statistic: median; no separate warm-up",
            "Timing includes algorithm allocations; excludes generation and validation.",
            "DFS edgeScans exclude reverse-graph construction; DAG edgeScans count relaxation only."));
        System.out.println("PASS: all 10 reference triples, 50 core rows, 10 bonus rows; deterministic counters.");
    }

    private static void write(Path target,List<String> lines) throws IOException {
        Path temp = target.resolveSibling(target.getFileName()+".tmp");
        Files.write(temp,lines,StandardCharsets.UTF_8);
        try {
            Files.move(temp,target,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp,target,StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
