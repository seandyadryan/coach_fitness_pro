"""Capture real Android views and exercise the main flows on a dedicated emulator.

This clears this app's data on the selected emulator. Do not point at a personal
device. Set ANDROID_SERIAL to select the dedicated QA emulator if necessary.
"""
from pathlib import Path
import json, os, re, shutil, subprocess, time, xml.etree.ElementTree as ET
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ADB=shutil.which('adb') or str(Path(os.environ.get('ANDROID_HOME', str(Path.home()/'AppData/Local/Android/Sdk')))/'platform-tools/adb.exe')
PACKAGE='com.deploydulupulangnanti.coachfitness'
DEST=ROOT/'store/screenshots'
RESULTS=[]
def adb(*args, binary=False):
    result=subprocess.run([str(ADB),*args],capture_output=True,timeout=45,check=True)
    return result.stdout if binary else result.stdout.decode('utf-8',errors='replace').strip()
def shell(*args): return adb('shell',*args)
def nodes():
    for attempt in range(4):
        try:
            shell('uiautomator','dump','/sdcard/coach-ui.xml')
            return ET.fromstring(shell('cat','/sdcard/coach-ui.xml'))
        except (subprocess.CalledProcessError,ET.ParseError,subprocess.TimeoutExpired):
            if attempt==3: raise
            time.sleep(2)
def find(value):
    tree=nodes()
    options=[n for n in tree.iter('node') if n.get('content-desc','').casefold()==value.casefold()]
    options += [n for n in tree.iter('node') if n.get('text','').casefold()==value.casefold()]
    return options[0] if options else None
def click_node(node):
    x1,y1,x2,y2=map(int,re.findall(r'\d+',node.get('bounds')))
    shell('input','tap',str((x1+x2)//2),str((y1+y2)//2)); time.sleep(.4)
def confirm_dialog():
    positive=next((n for n in nodes().iter('node') if n.get('resource-id')=='android:id/button1'),None)
    assert positive is not None,'Dialog positive button missing'
    click_node(positive)
def background_pause():
    shell('input','keyevent','3'); time.sleep(.4)
    shell('am','start','-W','-n',PACKAGE+'/.MainActivity'); time.sleep(.5)
    expect('Resume')
def tap(value, scroll=False, click=True):
    for _ in range(9 if scroll else 2):
        node=find(value)
        if node is not None:
            x1,y1,x2,y2=map(int,re.findall(r'\d+',node.get('bounds')))
            if x2>x1 and y2>y1:
                if click: shell('input','tap',str((x1+x2)//2),str((y1+y2)//2)); time.sleep(.4)
                return
        if scroll:
            width,height=display_size()
            shell('input','swipe',str(width//2),str(int(height*.73)),str(width//2),str(int(height*.28)),'250')
    raise AssertionError(f'UI element missing: {value}')
def display_size():
    text=shell('wm','size'); return tuple(map(int,re.findall(r'(\d+)x(\d+)',text)[-1]))
def expect(value):
    assert find(value) is not None,f'Expected UI text: {value}'
    RESULTS.append('PASS '+value)
def launch():
    shell('am','force-stop',PACKAGE); shell('am','start','-W','-n',PACKAGE+'/.MainActivity'); time.sleep(2)
def capture(category,name):
    path=DEST/category/(name+'.png'); path.parent.mkdir(parents=True,exist_ok=True)
    assert sum(1 for n in nodes().iter('node') if n.get('package')==PACKAGE)>5,'App UI not ready for capture'
    time.sleep(.5); path.write_bytes(adb('exec-out','screencap','-p',binary=True))
    with Image.open(path) as image:
        w,h=image.size
        assert w*9==h*16 or w*16==h*9,(path,w,h)
        assert path.stat().st_size<8*1024*1024
        assert min(w,h)>= (1080 if category in ['desktop','tablet-10'] else 320)
    print('Captured',category,name,flush=True)
def configure(w,h,density):
    shell('wm','size',f'{w}x{h}'); shell('wm','density',str(density)); time.sleep(1); launch()
def main():
    assert 'emulator-' in adb('get-serialno'), 'Use a dedicated emulator, not a physical device'
    shell('pm','clear',PACKAGE)
    for setting in ['window_animation_scale','transition_animation_scale','animator_duration_scale']: shell('settings','put','global',setting,'0')
    shell('settings','put','system','font_scale','1.0')
    shell('settings','put','global','sysui_demo_allowed','1')
    shell('am','broadcast','-a','com.android.systemui.demo','-e','command','clock','-e','hhmm','0941')
    shell('am','broadcast','-a','com.android.systemui.demo','-e','command','battery','-e','level','100','-e','plugged','false')
    configure(1080,1920,360)
    capture('phone','01-today')
    tap('Workouts'); expect('Find your next challenge.'); capture('phone','02-workouts')
    tap('Cardio'); expect('Everyday Energy'); assert find('Full Body Foundation') is None; RESULTS.append('PASS workout category filter')
    tap('All'); tap('Full Body Foundation'); expect('THE GAME PLAN'); capture('phone','03-workout-plan')
    tap('Start workout',scroll=True); expect('Move with care'); confirm_dialog(); time.sleep(2)
    background_pause(); expect('March in place'); RESULTS.append('PASS background automatically pauses timer')
    pause_node=find('Resume'); click_node(pause_node); time.sleep(2); click_node(pause_node)
    expect('Resume'); RESULTS.append('PASS manual pause and resume controls'); capture('phone','04-session')
    clocks=[n for n in nodes().iter('node') if re.fullmatch(r'\d\d:\d\d',n.get('text',''))]
    def text_height(node):
        _,y1,_,y2=map(int,re.findall(r'\d+',node.get('bounds'))); return y2-y1
    clock_before=max(clocks,key=text_height).get('text')
    time.sleep(2)
    assert find(clock_before) is not None; RESULTS.append('PASS paused timer does not decrement')
    launch(); tap('Resume your session'); expect('Resume'); RESULTS.append('PASS active session survives process restart paused')
    tap('End session',scroll=True); confirm_dialog(); expect('Show up.\nGet stronger.')
    tap('Workouts'); tap('Mobility'); tap('The 6-Minute Reset'); tap('Start workout',scroll=True); confirm_dialog()
    time.sleep(3); background_pause()
    # Exercise the supported skip controls to complete a genuine QA session.
    for _ in range(4): tap('Next exercise'); tap('Skip rest')
    tap('Finish workout'); expect('You showed up.'); tap('Progress'); expect('The 6-Minute Reset'); RESULTS.append('PASS completed session journal')
    capture('phone','05-progress')
    tap('Profile'); expect('Make it your own.'); capture('phone','06-profile')
    tap('Today'); tap('Add 250 ml',scroll=True); tap('Add 250 ml',scroll=True,click=False); expect('250 / 2000 ml'); RESULTS.append('PASS water increment')
    launch(); tap('Add 250 ml',scroll=True,click=False); expect('250 / 2000 ml'); RESULTS.append('PASS water persists across restart')
    launch(); tap('Exercise library',scroll=True); expect('Learn the movement. Own your form.'); capture('phone','07-exercise-library')
    for category,w,h,density in [('tablet-7',1280,720,160),('tablet-10',1920,1080,240),('desktop',1920,1080,160)]:
        configure(w,h,density); capture(category,'01-today')
        tap('Workouts'); capture(category,'02-workouts')
        tap('Progress'); capture(category,'03-progress')
        tap('Profile'); capture(category,'04-profile')
    configure(1080,1920,360)
    tap('Profile'); tap('Delete all my data',scroll=True); tap('Cancel'); tap('Progress'); expect('The 6-Minute Reset'); RESULTS.append('PASS deletion cancellation retains data')
    tap('Profile'); tap('Delete all my data',scroll=True); tap('Delete'); tap('Progress'); expect('Your story starts with one session.'); RESULTS.append('PASS delete all data clears journal')
    # Make the promotional video from the real app; no fabricated UI frames.
    video=ROOT/'store/video'; video.mkdir(parents=True,exist_ok=True)
    launch()
    recorder=subprocess.Popen([str(ADB),'shell','screenrecord','--bit-rate','3000000','--time-limit','30','/sdcard/coach-fitness-pro-preview.mp4'])
    time.sleep(4); tap('Workouts'); time.sleep(3); tap('Full Body Foundation'); time.sleep(4)
    shell('input','keyevent','4'); tap('Progress'); time.sleep(3); tap('Profile'); time.sleep(3)
    recorder.wait(timeout=45)
    adb('pull','/sdcard/coach-fitness-pro-preview.mp4',str(video/'coach-fitness-pro-preview.mp4'))
    errors=shell('dumpsys','activity','exit-info',PACKAGE)
    (ROOT/'store'/'qa-results.json').write_text(json.dumps(dict(device='Android API 35 x86_64 dedicated emulator',screenshots=len(list(DEST.rglob('*.png'))),checks=RESULTS,video='Native app recording; encoded duration in asset-validation.json',exit_info=errors),indent=2),encoding='utf-8')
    print('\n'.join(RESULTS),flush=True)
if __name__=='__main__':
    try: main()
    except Exception:
        debug=ROOT/'store/debug'; debug.mkdir(parents=True,exist_ok=True)
        try:
            (debug/'failure.png').write_bytes(adb('exec-out','screencap','-p',binary=True))
            (debug/'failure.xml').write_text(shell('cat','/sdcard/coach-ui.xml'),encoding='utf-8')
            (debug/'crash-log.txt').write_text(adb('logcat','-d','-b','crash','-t','100'),encoding='utf-8')
        except Exception: pass
        raise
