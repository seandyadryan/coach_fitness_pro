"""Native UI checks for reduced-motion controls, playback and session coupling."""
from pathlib import Path
from PIL import Image, ImageChops
import io,json,subprocess,time
import capture_screenshots as ui

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'artifacts/movement-qa'; OUT.mkdir(parents=True,exist_ok=True)
checks=[]
def demo_node():
    return next((node for node in ui.nodes().iter('node') if node.get('resource-id')==ui.PACKAGE+':id/movement_demo'),None)
def picture():
    import re
    node=demo_node(); assert node is not None,'Movement illustration missing'
    box=tuple(map(int,re.findall(r'\d+',node.get('bounds'))))
    return Image.open(io.BytesIO(ui.adb('exec-out','screencap','-p',binary=True))).convert('RGB').crop(box)
def open_exercise(name):
    ui.launch(); ui.tap('Exercise library',scroll=True); ui.tap(name,scroll=True)
    ui.expect('MOVEMENT DEMO'); assert demo_node() is not None
def changed(a,b): return ImageChops.difference(a,b).getbbox() is not None
def main():
    assert 'emulator-' in ui.adb('get-serialno')
    ui.shell('settings','put','global','animator_duration_scale','0')
    for name,slug in [('Bodyweight squat','squat'),('Push-up','pushup'),('Dumbbell curl','curl')]:
        open_exercise(name); ui.expect('Next pose')
        a=picture(); time.sleep(.6); assert not changed(a,picture()),'Reduced motion unexpectedly autoplays'
        ui.tap('Next pose'); b=picture(); assert changed(a,b),'Manual pose control did not change the native illustration'
        a.save(OUT/(slug+'-a.png')); b.save(OUT/(slug+'-b.png'))
        checks.append('PASS reduced-motion still frame and manual pose stepping: '+name)
    ui.shell('settings','put','global','animator_duration_scale','1')
    open_exercise('Dumbbell curl'); ui.expect('Pause demo')
    a=picture(); time.sleep(1); assert changed(a,picture()),'Preview is not animating'
    ui.tap('Pause demo'); ui.expect('Play demo'); a=picture(); time.sleep(.6); assert not changed(a,picture()),'Paused preview changed'
    ui.tap('Play demo'); checks.append('PASS preview plays, pauses and resumes')
    video=ROOT/'store/animations'; video.mkdir(parents=True,exist_ok=True)
    recorder=subprocess.Popen([str(ui.ADB),'shell','screenrecord','--bit-rate','4000000','--time-limit','8','/sdcard/movement-demo.mp4'])
    recorder.wait(timeout=20)
    ui.adb('pull','/sdcard/movement-demo.mp4',str(video/'movement-demo.mp4'))
    ui.launch(); ui.tap('Start workout'); ui.tap('Start workout',scroll=True); ui.confirm_dialog(); ui.background_pause()
    a=picture(); time.sleep(.6); assert not changed(a,picture()),'Paused session illustration moved'
    ui.tap('Next exercise'); ui.expect('REST & RESET'); assert demo_node() is None,'Rest state is showing an active exercise'
    ui.tap('End session',scroll=True); ui.confirm_dialog()
    checks.append('PASS session pause freezes its illustration; rest hides exercise motion')
    ui.shell('settings','put','global','animator_duration_scale','0')
    (ROOT/'store/movement-qa.json').write_text(json.dumps(dict(device='Android API 35 x86_64 emulator',checks=checks,native_video='animations/movement-demo.mp4'),indent=2),encoding='utf-8')
    print('\n'.join(checks),flush=True)
if __name__=='__main__': main()
