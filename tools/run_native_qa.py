"""Run focused movement QA, or refresh the complete baseline captures first."""
from pathlib import Path
import argparse,subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(); parser.add_argument('--mode',choices=['movements','full'],default='movements'); args=parser.parse_args()
if args.mode=='full': subprocess.run([sys.executable,str(ROOT/'tools/capture_screenshots.py')],check=True)
subprocess.run([sys.executable,str(ROOT/'tools/check_movement_demos.py')],check=True)
subprocess.run([sys.executable,str(ROOT/'tools/validate_package.py')],check=True)
