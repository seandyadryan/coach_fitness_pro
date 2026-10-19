"""Deterministic store artwork packaging. Actual screenshots are captured separately."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'store' / 'graphics'
OUT.mkdir(parents=True, exist_ok=True)
LIME = '#c2f86a'
BG = '#101411'
def font(size, bold=False):
    return ImageFont.truetype('C:/Windows/Fonts/' + ('arialbd.ttf' if bold else 'arial.ttf'), size)
def mark(size=512):
    im = Image.new('RGB', (size,size), BG)
    d = ImageDraw.Draw(im)
    u=size/512
    def box(coords, fill, radius=0):
        rect=tuple(int(c*u) for c in coords)
        if radius: d.rounded_rectangle(rect, radius=int(radius*u), fill=fill)
        else: d.rectangle(rect, fill=fill)
    box((72,72,440,440),LIME,88)
    for rect in [(142,233,370,279),(170,176,206,336),(306,176,342,336),(134,211,158,301),(354,211,378,301)]: box(rect,BG,8)
    return im
mark().save(OUT/'app-icon-512.png', optimize=True)
hero=Image.open(ROOT/'app/src/main/res/drawable-nodpi/training_hero.png').convert('RGB')
from PIL import ImageOps
feature=ImageOps.fit(hero,(1024,500))
d=ImageDraw.Draw(feature)
# Dark left field gives reliable title contrast.
shade=Image.new('RGBA',feature.size)
sd=ImageDraw.Draw(shade)
for x in range(1024): sd.line((x,0,x,500), fill=(16,20,17,int(245*max(0,1-x/720))))
feature=Image.alpha_composite(feature.convert('RGBA'),shade).convert('RGB')
d=ImageDraw.Draw(feature)
feature.paste(mark(62),(48,46))
d.text((126,59),'COACH FITNESS PRO',font=font(24,True),fill=LIME)
d.text((48,163),'Show up.',font=font(66,True),fill='#f3f5ec')
d.text((48,238),'Get stronger.',font=font(66,True),fill='#f3f5ec')
d.text((50,356),'YOUR EVERYDAY ADVANTAGE',font=font(17,True),fill=LIME)
d.text((50,395),'Guided workouts. Personal progress.',font=font(20),fill='#d1dacd')
feature.save(OUT/'feature-graphic-1024x500.png',optimize=True)
# Legacy fallback, while Android 26+ uses the native adaptive vector icon.
for bucket,size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    path=ROOT/f'app/src/main/res/mipmap-{bucket}'; path.mkdir(parents=True,exist_ok=True)
    mark(size).save(path/'ic_launcher.png')
print('Created icon, adaptive-icon fallbacks and 1024x500 feature graphic.')
