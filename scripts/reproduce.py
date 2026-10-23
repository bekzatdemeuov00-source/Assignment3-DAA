"""One command: tests, full benchmark, plots, and the five-page report."""
import os
from pathlib import Path
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
os.chdir(ROOT)
wrapper = str(ROOT / ("mvnw.cmd" if os.name == "nt" else "mvnw"))
if not os.environ.get("JAVA_HOME"):
    candidates = sorted(Path("C:/Program Files/Java").glob("jdk*")) if os.name == "nt" else sorted(Path("/usr/lib/jvm").glob("*"))
    java_home = next((p for p in candidates if (p / "bin" / ("java.exe" if os.name == "nt" else "java")).exists()), None)
    if java_home:
        os.environ["JAVA_HOME"] = str(java_home)
        os.environ["PATH"] = str(java_home / "bin") + os.pathsep + os.environ["PATH"]
arguments = [wrapper,"-B","-ntp","-Dmaven.repo.local=.tools/repository","clean","verify","-Pbenchmark",*sys.argv[1:]]
subprocess.run(arguments,check=True)
subprocess.run([sys.executable,str(ROOT/"scripts/analyze.py")],check=True)
print("Complete: tests, references, CSVs, ratios, slopes, and plots. See REPORT.md.")
