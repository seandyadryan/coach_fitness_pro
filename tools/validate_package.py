"""Validate shipped store files without altering them."""
from pathlib import Path
from PIL import Image
import hashlib,json,struct
ROOT=Path(__file__).resolve().parents[1]
store=ROOT/'store'
rows=json.loads((store/'listing-translations.json').read_text(encoding='utf-8'))
assert len(rows)==86 and len({x['locale'] for x in rows})==86
for row in rows:
    assert 0<len(row['title'])<=30
    assert 0<len(row['short_description'])<=80
    assert 0<len(row['full_description'])<=4000
    assert 'seandyadryan2@gmail.com' in row['full_description']
    assert 'PT Deploy Dulu Pulang Nanti' in row['full_description']
report={'listing_locales':len(rows),'english_short_characters':len((store/'short-description.txt').read_text().strip()),'english_full_characters':len((store/'full-description.txt').read_text().strip()),'images':[]}
for name,size in [('app-icon-512.png',(512,512)),('feature-graphic-1024x500.png',(1024,500))]:
    path=store/'graphics'/name
    with Image.open(path) as im: assert im.size==size and im.format=='PNG'
    assert path.stat().st_size<15*1024*1024
    report['images'].append(dict(file=str(path.relative_to(store)),width=size[0],height=size[1],bytes=path.stat().st_size))
for category,count,minimum,maximum in [('phone',None,320,3840),('tablet-7',4,320,3840),('tablet-10',4,1080,7680),('desktop',4,1080,7680)]:
    files=list((store/'screenshots'/category).glob('*.png')); assert (2<=len(files)<=8 if count is None else len(files)==count),(category,len(files))
    for path in sorted(files):
        with Image.open(path) as im:
            w,h=im.size; assert im.format=='PNG'
            assert (w*9==h*16 or w*16==h*9) and min(w,h)>=minimum and max(w,h)<=maximum
        assert path.stat().st_size<=8*1024*1024
        report['images'].append(dict(file=str(path.relative_to(store)),width=w,height=h,bytes=path.stat().st_size))
video=store/'video/coach-fitness-pro-preview.mp4'; assert video.exists() and video.stat().st_size>10000
report['video_bytes']=video.stat().st_size
def boxes(blob):
    offset=0
    while offset+8<=len(blob):
        size,kind=struct.unpack('>I4s',blob[offset:offset+8]); header=8
        if size==1: size=struct.unpack('>Q',blob[offset+8:offset+16])[0]; header=16
        if size==0: size=len(blob)-offset
        assert size>=header and offset+size<=len(blob)
        yield kind,blob[offset+header:offset+size]
        offset+=size
def video_info(path):
    data=path.read_bytes()
    moov=next(payload for kind,payload in boxes(data) if kind==b'moov')
    mvhd=next(payload for kind,payload in boxes(moov) if kind==b'mvhd')
    scale,duration=struct.unpack('>IQ',mvhd[20:32]) if mvhd[0]==1 else struct.unpack('>II',mvhd[12:20])
    assert b'avc1' in data,'Expected H.264 video'
    track=next(payload for kind,payload in boxes(moov) if kind==b'trak')
    tkhd=next(payload for kind,payload in boxes(track) if kind==b'tkhd')
    width,height=struct.unpack('>II',tkhd[-8:]); width//=65536; height//=65536
    return dict(seconds=round(duration/scale,3),width=width,height=height,bytes=len(data))
main_video=video_info(video)
assert 20<=main_video['seconds']<=32,main_video
assert (main_video['width'],main_video['height'])==(1080,1920),main_video
report['video_seconds']=main_video['seconds']
report['video_dimensions']=[main_video['width'],main_video['height']]
report['screenshot_count']=len(report['images'])-2
animations=list((store/'animations').glob('*.gif'))
report['movement_animation_count']=len(animations)
connectivity_path=store/'character/native-checks/connectivity.json'
if connectivity_path.exists():
    connectivity=json.loads(connectivity_path.read_text(encoding='utf-8'))
    assert connectivity['status']=='PASS' and connectivity['pose_count']==168
    assert len({check['exercise'] for check in connectivity['checks']})==21
    assert all(check['status']=='PASS' and check['head_neck_torso_connected'] and not check['detached_joint_anchors'] for check in connectivity['checks'])
    report['native_character_connectivity']={'status':'PASS','poses':168,'version_name':connectivity['version_name'],'version_code':connectivity['version_code']}
character_manifest=store/'character/textures.json'
if character_manifest.exists():
    textures=json.loads(character_manifest.read_text(encoding='utf-8'))
    assert len(textures)==10
    character_files=[]
    for name,metadata in textures.items():
        path=ROOT/'app/src/main/res/drawable-nodpi'/f'athlete_{name}.webp'
        with Image.open(path) as image:
            assert image.mode=='RGBA' and image.getchannel('A').getextrema()==(0,255),path
            assert image.size==(metadata['width'],metadata['height']),path
        character_files.append(dict(part=name,bytes=path.stat().st_size,sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
    report['photo_character_textures']=character_files
if animations:
    expected=set('march squat wall pushup bridge bird plank lunge calf deadbug step knees jack box cat child rotation shoulder curl row goblet'.split())
    assert {path.stem for path in animations}==expected
    for path in animations:
        with Image.open(path) as im:
            assert im.size==(400,260) and im.n_frames>1 and im.info.get('loop')==0,path
            milliseconds=0
            for frame in range(im.n_frames): im.seek(frame); milliseconds+=im.info.get('duration',0)
            assert 3500<=milliseconds<=6500,(path,milliseconds)
movement_video=store/'animations/movement-demo.mp4'
if movement_video.exists():
    report['native_movement_video']=video_info(movement_video)
    assert report['native_movement_video']['seconds']>=5
    assert (report['native_movement_video']['width'],report['native_movement_video']['height'])==(1080,1920)
report['status']='PASS'
(store/'asset-validation.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in report.items() if k!='images'},indent=2))
