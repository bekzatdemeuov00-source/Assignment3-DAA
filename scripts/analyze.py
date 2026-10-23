"""Validate benchmark data, calculate fitted slopes, and render the three required PNGs."""
import csv
import math
import os
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
LOCAL_PACKAGES = ROOT / ".tools" / "python"
if LOCAL_PACKAGES.is_dir():
    sys.path.insert(0, str(LOCAL_PACKAGES))
os.environ.setdefault("MPLCONFIGDIR", str(ROOT / ".tools" / "matplotlib"))
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
LABELS = {"DagShortestPath": "DAG shortest paths"}
COLORS = {"Kruskal": "#1565c0", "Prim": "#e07a17", "Kosaraju": "#218c74",
          "Kahn": "#8e44ad", "DagShortestPath": "#c0392b"}

def read_csv(name):
    with (ROOT / "results" / name).open(newline="", encoding="utf-8-sig") as stream:
        rows = list(csv.DictReader(stream))
    for row in rows:
        for field in ["n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"]:
            row[field] = int(row[field])
        row["time_ms"] = float(row["time_ms"])
        row["density"] = row["m"] // row["n"]
        row["operations"] = row["dsu_ops"] + row["edge_scans"] + row["comparisons"]
    return rows

def fitted_slope(rows, field):
    pairs = [(math.log(r["n"]), math.log(r[field])) for r in rows if r[field] > 0]
    if len(pairs) < 2:
        return None
    x_mean = sum(x for x, _ in pairs) / len(pairs)
    y_mean = sum(y for _, y in pairs) / len(pairs)
    return sum((x-x_mean)*(y-y_mean) for x,y in pairs) / sum((x-x_mean)**2 for x,_ in pairs)

def validate():
    rows = read_csv("results.csv")
    bonus = read_csv("bonus_results.csv")
    expected_source = (ROOT / "src/main/java/daa/benchmark/ReferenceValues.java").read_text()
    expected = [tuple(map(int, group)) for group in re.findall(
        r"new Expected\((\d+),\s*(\d+),\s*(\d+),\s*(\d+),\s*(\d+)\)", expected_source)]
    assert len(expected) == 10 and len(rows) == 50 and len(bonus) == 10
    lookup = {(r["algorithm"],r["n"],r["m"]): r for r in rows + bonus}
    assert len(lookup) == 60, "Duplicate rows"
    for n,m,mst,scc,dist in expected:
        values = {"Kruskal": mst, "Prim": mst, "Kosaraju": scc,
                  "Kahn": n, "DagShortestPath": dist, "Tarjan": scc}
        for algorithm, result in values.items():
            row = lookup[algorithm,n,m]
            assert row["result"] == result, (algorithm,n,m,row["result"],result)
            assert row["time_ms"] >= 0
            assert all(row[field] >= 0 for field in ["dsu_ops","edge_scans","comparisons","max_depth"])
            if algorithm != "Kruskal":
                assert row["edge_scans"] == m * (2 if algorithm in ["Prim","Kosaraju"] else 1)
            else:
                assert n-1 <= row["edge_scans"] <= m
                assert row["dsu_ops"] == 2*row["edge_scans"] + n-1
            if algorithm in ["Kosaraju","Tarjan"]:
                assert 1 <= row["max_depth"] <= n
    return rows, bonus, expected

def groups(rows):
    return {(a,d): sorted([r for r in rows if r["algorithm"] == a and r["density"] == d],
                          key=lambda r: r["n"])
            for a in ALGORITHMS for d in [4,20]}

def main():
    rows, bonus, expected = validate()
    grouped = groups(rows)
    out = ROOT / "results"
    plots = out / "plots"
    plots.mkdir(parents=True, exist_ok=True)
    normalized = []
    slopes = []
    for (algorithm,density), group in grouped.items():
        for row in group:
            if algorithm in ["Kruskal","Prim"]:
                counter = row["comparisons"]
                formula = row["m"] * math.log2(row["m"])
                normalizer = "comparisons / (m log2 m)"
            else:
                counter = row["edge_scans"]
                formula = row["n"] + row["m"]
                normalizer = "edge_scans / (n + m)"
            normalized.append({"algorithm":algorithm,"density":density,"n":row["n"],"m":row["m"],
                "normalizer":normalizer,"ratio":counter/formula,
                "scans_per_edge":row["edge_scans"]/row["m"],
                "dsu_per_edge":row["dsu_ops"]/row["m"]})
        slopes.append({"algorithm":algorithm,"density":density,
            "edge_scans_slope":fitted_slope(group,"edge_scans"),
            "comparisons_slope":fitted_slope(group,"comparisons"),
            "operations_slope":fitted_slope(group,"operations")})
    for name,data in [("normalized.csv",normalized),("slopes.csv",slopes)]:
        with (out/name).open("w",newline="",encoding="utf-8") as stream:
            writer = csv.DictWriter(stream,fieldnames=list(data[0]))
            writer.writeheader()
            writer.writerows(data)

    plt.rcParams.update({"font.family":"DejaVu Sans","font.size":10,
        "axes.spines.top":False,"axes.spines.right":False,"axes.titleweight":"bold",
        "figure.facecolor":"white","savefig.facecolor":"white"})
    fig, axes = plt.subplots(1,2,figsize=(10,3.65),layout="constrained")
    for axis,density in zip(axes,[4,20]):
        for algorithm in ALGORITHMS:
            group = grouped[algorithm,density]
            axis.loglog([r["n"] for r in group],[r["operations"] for r in group],
                marker="o",markersize=4,color=COLORS[algorithm],
                label=LABELS.get(algorithm,algorithm))
        axis.set(title=("Sparse" if density == 4 else "Dense") + f": m = {density}n",
                 xlabel="Vertices n",ylabel="Counted operations")
        axis.grid(True,which="both",alpha=.18)
        axis.legend(fontsize=8,loc="upper left")
    fig.savefig(plots/"operations.png",dpi=200)
    plt.close(fig)

    fig, axes = plt.subplots(1,2,figsize=(10,3.05),layout="constrained")
    for axis,density in zip(axes,[4,20]):
        x = [r["n"] for r in grouped["Kruskal",density]]
        axis.semilogx(x,[2]*5,"s-",color=COLORS["Kosaraju"],label="Prim / Kosaraju = 2")
        axis.semilogx(x,[1]*5,"^-",color=COLORS["Kahn"],label="Kahn / DAG relaxation = 1")
        axis.semilogx(x,[r["edge_scans"]/r["m"] for r in grouped["Kruskal",density]],
                     "o-",color=COLORS["Kruskal"],label="Kruskal")
        axis.set(title=f"m = {density}n",xlabel="Vertices n",ylabel="edgeScans / m",ylim=(0,2.3))
        axis.grid(True,which="both",alpha=.18)
        axis.legend(fontsize=8,loc="lower right")
    fig.savefig(plots/"scans_per_edge.png",dpi=200)
    plt.close(fig)

    fig, axis = plt.subplots(figsize=(10,3.05),layout="constrained")
    for density,color,marker in [(4,"#1565c0","o"),(20,"#e07a17","s")]:
        group = grouped["Kruskal",density]
        axis.semilogx([r["n"] for r in group],[r["edge_scans"]/r["m"] for r in group],
                      marker=marker,color=color,label=f"m = {density}n",linewidth=2)
    axis.set(title="Fraction of sorted edges examined by Kruskal",
             xlabel="Vertices n",ylabel="edgeScans / m",ylim=(0,1.08))
    axis.grid(True,which="both",alpha=.18)
    axis.legend(loc="lower right")
    fig.savefig(plots/"kruskal_share.png",dpi=200)
    plt.close(fig)
    for row in slopes:
        print(row)
    print("PASS: 50 core rows, 10 bonus rows, all reference values, all exact scan identities.")
    return rows,bonus,expected,normalized,slopes

if __name__ == "__main__":
    main()
