"""Composite photo-character GIFs from production Kotlin texture placements.

These are animation previews. The native MP4 and QA PNGs capture the Android View.
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont,ImageEnhance,ImageChops
import argparse,json,math
ROOT=Path(__file__).resolve().parents[1]
S=2
TEXTURES={path.stem.removeprefix('athlete_'):Image.open(path).convert('RGBA') for path in (ROOT/'app/src/main/res/drawable-nodpi').glob('athlete_*.webp')}
PREMULTIPLIED={name:image.convert('RGBa') for name,image in TEXTURES.items() if name.startswith('body_')}
MESH_TEXTURES={}

def font(size,bold=False):
    choices=[Path('C:/Windows/Fonts')/('arialbd.ttf' if bold else 'arial.ttf'),Path('/usr/share/fonts/truetype/dejavu')/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')]
    for path in choices:
        if path.exists(): return ImageFont.truetype(str(path),int(size*S))
    return ImageFont.load_default()

def mesh_triangle(stage,source,destination,uv):
    # Rasterize the production Android mesh with inverse affine texture sampling.
    (x0,y0),(x1,y1),(x2,y2)=destination
    determinant=(x1-x0)*(y2-y0)-(x2-x0)*(y1-y0)
    if abs(determinant)<1e-6: return
    left=max(0,math.floor(min(x0,x1,x2))); top=max(0,math.floor(min(y0,y1,y2)))
    right=min(stage.width,math.ceil(max(x0,x1,x2))+1); bottom=min(stage.height,math.ceil(max(y0,y1,y2))+1)
    if right<=left or bottom<=top: return
    def coefficients(values):
        v0,v1,v2=values
        a=((v1-v0)*(y2-y0)-(v2-v0)*(y1-y0))/determinant
        b=((x1-x0)*(v2-v0)-(x2-x0)*(v1-v0))/determinant
        return a,b,v0-a*x0-b*y0+a*left+b*top
    a=coefficients([p[0]*(source.width-1) for p in uv]); b=coefficients([p[1]*(source.height-1) for p in uv])
    tile=source.transform((right-left,bottom-top),Image.Transform.AFFINE,(*a,*b),Image.Resampling.BICUBIC).convert('RGBA')
    mask=Image.new('L',tile.size)
    ImageDraw.Draw(mask).polygon([(x-left,y-top) for x,y in destination],fill=255)
    tile.putalpha(ImageChops.multiply(tile.getchannel('A'),mask)); stage.alpha_composite(tile,(left,top))

def photo_sprite(stage,sprite):
    source=TEXTURES[sprite['part']]
    if sprite.get('mesh'):
        key=sprite['part']
        if key not in MESH_TEXTURES:
            source=TEXTURES[key].copy(); values=sprite.get('mesh_opacity') or [255]*65
            mask=Image.new('L',source.size); draw=ImageDraw.Draw(mask)
            for y in range(source.height):
                position=y/max(1,source.height-1)*(len(values)-1); row=min(int(position),len(values)-2); fraction=position-row
                alpha=round(values[row]*(1-fraction)+values[row+1]*fraction)
                draw.line((0,y,source.width,y),fill=alpha)
            source.putalpha(ImageChops.multiply(source.getchannel('A'),mask))
            MESH_TEXTURES[key]=source.convert('RGBa')
        source=MESH_TEXTURES[key]
        points=[(x*S,y*S) for x,y in sprite['mesh']]
        rows=len(points)//3-1
        for row in range(rows):
            for col in range(2):
                a=row*3+col; b=a+1; c=a+3; d=c+1
                u0=col/2; u1=(col+1)/2; v0=row/rows; v1=(row+1)/rows
                mesh_triangle(stage,source,[points[a],points[b],points[c]],[(u0,v0),(u1,v0),(u0,v1)])
                mesh_triangle(stage,source,[points[b],points[d],points[c]],[(u1,v0),(u1,v1),(u0,v1)])
        return
    w=max(1,round(sprite['width']*S)); h=max(1,round(sprite['height']*S))
    part=source.resize((w,h),Image.Resampling.LANCZOS)
    if sprite['shade']<1:
        alpha=part.getchannel('A'); part=ImageEnhance.Brightness(part).enhance(sprite['shade']); part.putalpha(alpha)
    # Reproduce the production torso mesh as narrow horizontal texture strips.
    body=sprite['part'].startswith('body_')
    side=sprite['part']=='body_side'
    padding=math.ceil(abs(sprite['bend'])*S+w*.1)+2 if body else 0
    warped=Image.new('RGBA',(w+padding*2,h)) if body else part
    if body:
        for row in range(h):
            t=max(0,min(1,(row/max(1,h-1)-.44)/.5))
            offset=round(-sprite['bend']*S*4*t*(1-t)-(w*.1*t*t*(3-2*t) if side else 0))
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
