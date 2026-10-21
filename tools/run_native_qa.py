"""Run focused movement QA, or refresh the complete baseline captures first."""
from pathlib import Path
import argparse,subprocess,sys
import capture_screenshots as ui
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser(); parser.add_argument('--mode',choices=['movements','full'],default='movements'); args=parser.parse_args()
try:
    if args.mode=='full': subprocess.run([sys.executable,str(ROOT/'tools/capture_screenshots.py')],check=True)
    subprocess.run([sys.executable,str(ROOT/'tools/check_movement_demos.py')],check=True)
    subprocess.run([sys.executable,str(ROOT/'tools/validate_package.py')],check=True)
except Exception:
    debug=ROOT/'store/debug'; debug.mkdir(parents=True,exist_ok=True)
    for name,command,binary in [('screen.png',('exec-out','screencap','-p'),True),('activity.txt',('shell','dumpsys','activity','activities'),False),('logcat.txt',('logcat','-d','-t','500'),False),('window.txt',('shell','dumpsys','window'),False)]:
        try:
            data=ui.adb(*command,binary=binary)
            (debug/name).write_bytes(data) if binary else (debug/name).write_text(data,encoding='utf-8')
        except Exception: pass
    try:
        ui.shell('uiautomator','dump','/sdcard/coach-ui-failure.xml')
        (debug/'ui.xml').write_text(ui.shell('cat','/sdcard/coach-ui-failure.xml'),encoding='utf-8')
    except Exception: pass
    raise
