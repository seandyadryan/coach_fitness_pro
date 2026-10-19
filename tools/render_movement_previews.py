"""Render GIF previews using coordinates exported from the production Kotlin model.

These are illustration assets, not fabricated screenshots of the Android UI.
The native Android MP4 and native QA screenshots verify the actual View renderer.
"""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
import argparse,json,math
ROOT=Path(__file__).resolve().parents[1]
S=2
COLORS={'bg':'#18231c','floor':'#4a614b','skin':'#dae8d0','far':'#6e8b76','lime':'#c2f86a','leg':'#789977','farleg':'#3e5a46','shirt':'#66856a','shoe':'#d8e5cd','hair':'#15231b'}
def font(size,bold=False):
    choices=[Path('C:/Windows/Fonts')/('arialbd.ttf' if bold else 'arial.ttf'),Path('/usr/share/fonts/truetype/dejavu')/('DejaVuSans-Bold.ttf' if bold else 'DejaVuSans.ttf')]
    for path in choices:
        if path.exists(): return ImageFont.truetype(str(path),int(size*S))
    return ImageFont.load_default()
def render(spec,frame,index):
    im=Image.new('RGB',(400*S,260*S),COLORS['bg']); d=ImageDraw.Draw(im); p=frame['joints']
    def xy(point): return tuple(v*S for v in point)
    def ellipse(box,color): d.ellipse(tuple(v*S for v in box),fill=color)
    def line(a,b,width,color):
        d.line((xy(a),xy(b)),fill=color,width=int(width*S))
        for x,y in (a,b): ellipse((x-width/2,y-width/2,x+width/2,y+width/2),color)
    def limb(a,b,c,near,leg=False):
        color=COLORS['leg' if near else 'farleg'] if leg else COLORS['skin' if near else 'far']
        line(p[a],p[b],15 if leg else 10,color); line(p[b],p[c],11 if leg else 8,color)
        if not leg: ellipse((p[b][0]-2,p[b][1]-2,p[b][0]+2,p[b][1]+2),COLORS['lime'] if near else '#6c8b62')
    ellipse((97,224,305,238),'#26392a'); line((65,233),(335,233),1,COLORS['floor'])
    if spec['prop']=='WALL': d.rounded_rectangle((300*S,52*S,306*S,233*S),radius=2*S,fill='#526b55')
    limb('LH','LK','LA',False,True); line(p['LA'],p['LT'],9,'#81977b')
    limb('LS','LE','LW',False)
    top=[(a+b)/2 for a,b in zip(p['LS'],p['RS'])]; bottom=[(a+b)/2 for a,b in zip(p['LH'],p['RH'])]
    dx,dy=bottom[0]-top[0],bottom[1]-top[1]; length=max(1,math.hypot(dx,dy)); nx,ny=-dy/length,dx/length
    chest=22 if spec['view']=='FRONT VIEW' else 16; waist=15 if spec['view']=='FRONT VIEW' else 13
    def edge(sign):
        a=[top[0]+nx*chest*sign,top[1]+ny*chest*sign]
        b=[top[0]+dx*.35+nx*chest*sign,top[1]+dy*.35+ny*chest*sign+frame['curve']]
        c=[bottom[0]-dx*.35+nx*waist*sign,bottom[1]-dy*.35+ny*waist*sign+frame['curve']]
        e=[bottom[0]+nx*waist*sign,bottom[1]+ny*waist*sign]
        return [[(1-t)**3*a[k]+3*(1-t)**2*t*b[k]+3*(1-t)*t*t*c[k]+t**3*e[k] for k in (0,1)] for t in [i/16 for i in range(17)]]
    d.polygon([xy(point) for point in edge(1)+list(reversed(edge(-1)))],fill=COLORS['shirt'])
    limb('RH','RK','RA',True,True); line(p['RA'],p['RT'],9,COLORS['shoe'])
    limb('RS','RE','RW',True)
    line(p['NECK'],p['HEAD'],9,COLORS['skin']); x,y=p['HEAD']; ellipse((x-12,y-15,x+12,y+15),COLORS['skin'])
    d.pieslice(((x-12)*S,(y-17)*S,(x+12)*S,(y+3)*S),180,355,fill=COLORS['hair'])
    eyes=[x+7] if spec['view']=='SIDE VIEW' else [x-4,x+4]
    for eye in eyes: ellipse((eye-1,y,eye+1,y+2),COLORS['hair'])
    if spec['prop']=='DUMBBELLS':
        for wrist in ['LW','RW']:
            x,y=p[wrist]; line((x-11,y),(x+11,y),3.5,COLORS['shoe'])
            for a,b in [(x-14,x-8),(x+8,x+14)]: d.rounded_rectangle((a*S,(y-9)*S,b*S,(y+9)*S),radius=2*S,fill='#94b383')
    if spec['prop']=='GOBLET':
        x,y=[(a+b)/2 for a,b in zip(p['LW'],p['RW'])]; line((x,y-10),(x,y+11),6,COLORS['shoe'])
        for a,b in [(y-15,y-6),(y+7,y+15)]: d.rounded_rectangle(((x-10)*S,a*S,(x+10)*S,b*S),radius=2*S,fill='#859f86')
    d.text((15*S,12*S),spec['name'],font=font(11,True),fill=COLORS['lime'])
    cue=spec['firstCue'] if index<len(spec['frames'])/2 else spec['secondCue']
    d.text((200*S,247*S),cue,font=font(10),anchor='mm',fill='#bed0b7')
    return im.resize((400,260),Image.Resampling.LANCZOS)
def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--frames',type=Path,default=ROOT/'app/build/reports/movement-frames.json'); parser.add_argument('--out',type=Path,default=ROOT/'store/animations'); args=parser.parse_args()
    specs=json.loads(args.frames.read_text(encoding='utf-8')); args.out.mkdir(parents=True,exist_ok=True)
    for spec in specs:
        frames=[render(spec,frame,index) for index,frame in enumerate(spec['frames'])]
        palette=frames[0].quantize(colors=128)
        frames=[frame.quantize(palette=palette,dither=Image.Dither.NONE) for frame in frames]
        count=len(frames); times=[round((i+1)*spec['cycleMs']/count/10)*10-round(i*spec['cycleMs']/count/10)*10 for i in range(count)]
        frames[0].save(args.out/(spec['id']+'.gif'),save_all=True,append_images=frames[1:],duration=times,loop=0,disposal=2,optimize=False)
    sheet=Image.new('RGB',(1200,7*260),'#101411')
    for index,spec in enumerate(specs): sheet.paste(render(spec,spec['frames'][12],12),((index%3)*400,(index//3)*260))
    sheet.save(ROOT/'.tools/movement-contact-sheet.jpg')
    print(f'Rendered {len(specs)} looping GIF previews from production geometry.')
if __name__=='__main__': main()
