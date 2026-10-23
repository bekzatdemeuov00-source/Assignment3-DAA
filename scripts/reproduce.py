"""One command: tests, full benchmark, plots, and the five-page report."""
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
os.chdir(ROOT)
wrapper = str(ROOT / ("mvnw.cmd" if os.name == "nt" else "mvnw"))
arguments = [wrapper,"-B","-ntp","clean","verify","-Pbenchmark",*sys.argv[1:]]
subprocess.run(arguments,check=True)
subprocess.run([sys.executable,str(ROOT/"scripts/analyze.py")],check=True)
subprocess.run([sys.executable,str(ROOT/"scripts/report.py")],check=True)
print("Complete: tests, references, CSVs, plots, REPORT.md, REPORT.pdf")
