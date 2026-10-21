"""Zoom and label unmodified native renders to review the reported neck gap."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1]
def main():
    old=Image.open(ROOT/'store/character/before-neck-gap.png').convert('RGB')
    scale=min(old.width/400,old.height/260)
    offset=((old.width-400*scale)/2,(old.height-260*scale)/2)
    world=(220,132,320,207)
    before=old.crop(tuple(round(offset[i%2]+value*scale) for i,value in enumerate(world)))
    current=Image.open(ROOT/'store/character/native-checks/pushup-3.png').convert('RGBA')
    background=Image.new('RGBA',current.size,'#18231c'); background.alpha_composite(current)
    after=background.convert('RGB').crop(tuple(round(value*2) for value in world))
    output=Image.new('RGB',(1260,570),'#101611'); draw=ImageDraw.Draw(output)
    font_path=Path('C:/Windows/Fonts/arialbd.ttf')
    if not font_path.exists(): font_path=Path('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf')
    font=ImageFont.truetype(str(font_path),26)
    for x,label,image in [(20,'Before: v1.2.0','before'),(650,'Fixed: v1.2.1','after')]:
        draw.text((x,20),label,fill='#c2f86a',font=font)
        crop=before if image=='before' else after
        output.paste(crop.resize((590,443),Image.Resampling.LANCZOS),(x,70))
    draw.text((20,525),'Same exercise and phase: push-up, 0.37',fill='#e5ebdc',font=font)
    path=ROOT/'store/character/neck-connection-comparison.png'; output.save(path)
    print(path)
if __name__=='__main__': main()
