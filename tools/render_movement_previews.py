"""Composite photo-character GIFs from production Kotlin texture placements.

These are animation previews. The native MP4 and QA PNGs capture the Android View.
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont,ImageEnhance
import argparse,json,math
ROOT=Path(__file__).resolve().parents[1]
S=2
TEXTURES={path.stem.removeprefix('athlete_'):Image.open(path).convert('RGBA') for path in (ROOT/'app/src/main/res/drawable-nodpi').glob('athlete_*.webp')}

def font(size,bold=False):
    choices=[Path('C:/Windows/Fonts')/('arialbd.ttf' if bold else 'arial.ttf'),Path('/usr/share/fonts/truetype/dejavu')/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')]
    for path in choices:
        if path.exists(): return ImageFont.truetype(str(path),int(size*S))
    return ImageFont.load_default()

def photo_sprite(stage,sprite):
    source=TEXTURES[sprite['part']]
    w=max(1,round(sprite['width']*S)); h=max(1,round(sprite['height']*S))
    part=source.resize((w,h),Image.Resampling.LANCZOS)
    if sprite['shade']<1:
        alpha=part.getchannel('A'); part=ImageEnhance.Brightness(part).enhance(sprite['shade']); part.putalpha(alpha)
    # Reproduce the production torso mesh as narrow horizontal texture strips.
    padding=math.ceil(abs(sprite['bend'])*S)+2
    warped=Image.new('RGBA',(w+padding*2,h))
    for row in range(h):
        t=row/max(1,h-1); offset=round(-sprite['bend']*S*4*t*(1-t))
        warped.alpha_composite(part.crop((0,row,w,row+1)),(padding+offset,row))
    px=padding+sprite['pivotX']*w; py=sprite['pivotY']*h
    size=math.ceil(max(warped.width,warped.height)*2.5)
    temp=Image.new('RGBA',(size,size)); center=size//2
    temp.alpha_composite(warped,(round(center-px),round(center-py)))
    temp=temp.rotate(-sprite['angle'],Image.Resampling.BICUBIC,center=(center,center))
    stage.alpha_composite(temp,(round(sprite['x']*S-center),round(sprite['y']*S-center)))

def render(spec,frame,index):
    im=Image.new('RGBA',(400*S,260*S),'#18231c'); d=ImageDraw.Draw(im)
    d.ellipse((97*S,224*S,305*S,238*S),fill='#26392a')
    d.line((65*S,233*S,335*S,233*S),fill='#4a614b',width=S)
    if spec['prop']=='WALL': d.rounded_rectangle((300*S,52*S,306*S,233*S),radius=2*S,fill='#526b55')
    for sprite in frame['sprites']: photo_sprite(im,sprite)
    d=ImageDraw.Draw(im); p=frame['joints']
    if spec['prop']=='DUMBBELLS':
        for wrist in ['LW','RW']:
            x,y=p[wrist]; d.line(((x-11)*S,y*S,(x+11)*S,y*S),width=round(3.5*S),fill='#dae5d0')
            for a,b in [(x-14,x-8),(x+8,x+14)]: d.rounded_rectangle((a*S,(y-9)*S,b*S,(y+9)*S),radius=2*S,fill='#94b383')
    if spec['prop']=='GOBLET':
        x,y=[(a+b)/2 for a,b in zip(p['LW'],p['RW'])]; d.line((x*S,(y-10)*S,x*S,(y+11)*S),width=6*S,fill='#dae5d0')
        for a,b in [(y-15,y-6),(y+7,y+15)]: d.rounded_rectangle(((x-10)*S,a*S,(x+10)*S,b*S),radius=2*S,fill='#859f86')
    d.text((15*S,12*S),spec['name'],font=font(11,True),fill='#c2f86a')
    cue=spec['firstCue'] if index<len(spec['frames'])/2 else spec['secondCue']
    d.text((200*S,247*S),cue,font=font(10),anchor='mm',fill='#bed0b7')
    return im.convert('RGB').resize((400,260),Image.Resampling.LANCZOS)

def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--frames',type=Path,default=ROOT/'app/build/reports/movement-frames.json'); parser.add_argument('--out',type=Path,default=ROOT/'store/animations'); parser.add_argument('--contact-only',action='store_true'); args=parser.parse_args()
    specs=json.loads(args.frames.read_text(encoding='utf-8')); args.out.mkdir(parents=True,exist_ok=True)
    if not args.contact_only:
        for spec in specs:
            frames=[render(spec,frame,index) for index,frame in enumerate(spec['frames'])]
            palette_source=Image.new('RGB',(400*4,260))
            for index in range(4): palette_source.paste(frames[index*12],(index*400,0))
            palette=palette_source.quantize(colors=256)
            frames=[frame.quantize(palette=palette,dither=Image.Dither.NONE) for frame in frames]
            count=len(frames); times=[round((i+1)*spec['cycleMs']/count/10)*10-round(i*spec['cycleMs']/count/10)*10 for i in range(count)]
            frames[0].save(args.out/(spec['id']+'.gif'),save_all=True,append_images=frames[1:],duration=times,loop=0,disposal=2,optimize=False)
            print('Rendered '+spec['id'],flush=True)
    sheet=Image.new('RGB',(1200,7*260),'#101411')
    for index,spec in enumerate(specs): sheet.paste(render(spec,spec['frames'][12],12),((index%3)*400,(index//3)*260))
    (ROOT/'.tools').mkdir(exist_ok=True); sheet.save(ROOT/'.tools/movement-contact-sheet.jpg')
    print(f'Rendered {len(specs)} photo-character previews from production texture placements.')
if __name__=='__main__': main()
