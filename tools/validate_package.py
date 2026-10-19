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
for category,count,minimum,maximum in [('phone',7,320,3840),('tablet-7',4,320,3840),('tablet-10',4,1080,7680),('desktop',4,1080,7680)]:
    files=list((store/'screenshots'/category).glob('*.png')); assert len(files)==count,(category,len(files))
    for path in sorted(files):
        with Image.open(path) as im:
            w,h=im.size; assert im.format=='PNG'
            assert (w*9==h*16 or w*16==h*9) and min(w,h)>=minimum and max(w,h)<=maximum
        assert path.stat().st_size<=8*1024*1024
        report['images'].append(dict(file=str(path.relative_to(store)),width=w,height=h,bytes=path.stat().st_size))
video=store/'video/coach-fitness-pro-preview.mp4'; assert video.exists() and video.stat().st_size>10000
report['video_bytes']=video.stat().st_size
data=video.read_bytes()
def boxes(blob):
    offset=0
    while offset+8<=len(blob):
        size,kind=struct.unpack('>I4s',blob[offset:offset+8]); header=8
        if size==1: size=struct.unpack('>Q',blob[offset+8:offset+16])[0]; header=16
        if size==0: size=len(blob)-offset
        assert size>=header and offset+size<=len(blob)
        yield kind,blob[offset+header:offset+size]
        offset+=size
moov=next(payload for kind,payload in boxes(data) if kind==b'moov')
mvhd=next(payload for kind,payload in boxes(moov) if kind==b'mvhd')
scale,duration=struct.unpack('>IQ',mvhd[20:32]) if mvhd[0]==1 else struct.unpack('>II',mvhd[12:20])
seconds=duration/scale
assert 20<=seconds<=32,seconds
assert b'avc1' in data,'Expected H.264 video'
track=next(payload for kind,payload in boxes(moov) if kind==b'trak')
tkhd=next(payload for kind,payload in boxes(track) if kind==b'tkhd')
width,height=struct.unpack('>II',tkhd[-8:]); width//=65536; height//=65536
assert (width,height)==(1080,1920),(width,height)
report['video_seconds']=round(seconds,3)
report['video_dimensions']=[width,height]
report['status']='PASS'
(store/'asset-validation.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in report.items() if k!='images'},indent=2))
