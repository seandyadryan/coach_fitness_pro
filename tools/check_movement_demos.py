"""Native UI checks for reduced-motion controls, playback and session coupling."""
from pathlib import Path
from PIL import Image, ImageChops
import io,json,re,subprocess,time
from datetime import datetime,timezone
import capture_screenshots as ui

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'store/animations/native-checks'; OUT.mkdir(parents=True,exist_ok=True)
checks=[]
def demo_node():
    return next((node for node in ui.nodes().iter('node') if node.get('resource-id')==ui.PACKAGE+':id/movement_demo'),None)
def picture(box=None):
    import re
    if box is None:
        node=demo_node(); assert node is not None,'Movement illustration missing'
        box=tuple(map(int,re.findall(r'\d+',node.get('bounds'))))
    return Image.open(io.BytesIO(ui.adb('exec-out','screencap','-p',binary=True))).convert('RGB').crop(box)
def open_exercise(name):
    ui.launch(); ui.tap('Exercise library',scroll=True); ui.tap(name,scroll=True)
    ui.expect('MOVEMENT DEMO'); assert demo_node() is not None
def changed(a,b):
    # Compare the athlete, excluding the moving phase ring and cue caption.
    width,height=a.size; scale=min(width/400,height/260); x=(width-400*scale)/2; y=(height-260*scale)/2
    box=tuple(round(value) for value in (x+40*scale,y+40*scale,x+325*scale,y+240*scale))
    return ImageChops.difference(a.crop(box),b.crop(box)).getbbox() is not None
def main():
    assert 'emulator-' in ui.adb('get-serialno')
    package_info=ui.shell('dumpsys','package',ui.PACKAGE)
    version_name=re.search(r'versionName=(\S+)',package_info).group(1)
    version_code=int(re.search(r'versionCode=(\d+)',package_info).group(1))
    ui.configure(1080,1920,360)
    ui.shell('settings','put','global','animator_duration_scale','0')
    for name,slug in [('Bodyweight squat','squat'),('Push-up','pushup'),('Dumbbell curl','curl')]:
        open_exercise(name); ui.expect('Next pose')
        a=picture(); time.sleep(.6); assert not changed(a,picture()),'Reduced motion unexpectedly autoplays'
        ui.tap('Next pose'); b=picture(); assert changed(a,b),'Manual pose control did not change the native illustration'
        a.save(OUT/(slug+'-a.png')); b.save(OUT/(slug+'-b.png'))
        checks.append('PASS reduced-motion still frame and manual pose stepping: '+name)
    ui.shell('settings','put','global','animator_duration_scale','1')
    open_exercise('Dumbbell curl'); ui.expect('Pause demo')
    a=picture(); time.sleep(.5); b=picture(); time.sleep(.7); c=picture()
    assert changed(a,b) or changed(a,c),'Preview is not animating'
    ui.tap('Pause demo'); ui.expect('Play demo'); a=picture(); time.sleep(.6); assert not changed(a,picture()),'Paused preview changed'
    ui.capture('phone','08-exercise-demo')
    ui.tap('Play demo'); checks.append('PASS preview plays, pauses and resumes')
    video=ROOT/'store/animations'; video.mkdir(parents=True,exist_ok=True)
    recorder=subprocess.Popen([str(ui.ADB),'shell','screenrecord','--bit-rate','4000000','--time-limit','8','/sdcard/movement-demo.mp4'])
    recorder.wait(timeout=20)
    ui.adb('pull','/sdcard/movement-demo.mp4',str(video/'movement-demo.mp4'))
    ui.launch(); ui.tap('Start workout'); ui.tap('Start workout',scroll=True); ui.confirm_dialog(); ui.background_pause()
    a=picture(); time.sleep(.6); assert not changed(a,picture()),'Paused session illustration moved'
    box=tuple(map(int,re.findall(r'\d+',demo_node().get('bounds'))))
    ui.tap('Resume'); a=picture(box); time.sleep(.7); assert changed(a,picture(box)),'Resumed session did not animate'
    ui.background_pause(); a=picture(); time.sleep(.6); assert not changed(a,picture()),'Background pause did not freeze the movement'
    ui.capture('phone','04-session')
    ui.tap('Next exercise'); ui.expect('REST & RESET'); assert demo_node() is None,'Rest state is showing an active exercise'
    ui.tap('End session',scroll=True); ui.confirm_dialog()
    checks.append('PASS session resume animates, pause/background freeze its illustration, and rest hides exercise motion')
    ui.shell('settings','put','global','animator_duration_scale','0')
    ui.launch(); ui.tap('Profile'); ui.capture('phone','06-profile')
    for category,w,h,density in [('tablet-7',1280,720,160),('tablet-10',1920,1080,240),('desktop',1920,1080,160)]:
        ui.configure(w,h,density); ui.tap('Profile'); ui.capture(category,'04-profile')
    ui.configure(1080,1920,360)
    (ROOT/'store/movement-qa.json').write_text(json.dumps(dict(device='Android API 35 x86_64 emulator',version_name=version_name,version_code=version_code,recorded_at=datetime.now(timezone.utc).isoformat(),checks=checks,native_video='animations/movement-demo.mp4'),indent=2),encoding='utf-8')
    print('\n'.join(checks),flush=True)
if __name__=='__main__': main()
