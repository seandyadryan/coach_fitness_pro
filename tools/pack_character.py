"""Mechanically crop the generated transparent atlas into native texture resources.

No artwork is synthesized or retouched here; preserve ImageGen's alpha channel.
"""
from pathlib import Path
from PIL import Image
import json
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'store/character/athlete-atlas.png'
BOXES={
    'upper_arm':(105,395,285,736), 'forearm':(495,397,625,738),
    'thigh':(815,400,1040,738), 'calf':(1220,401,1365,739),
    'hand':(85,742,290,1080), 'grip':(460,747,660,1020),
    'shoe_front':(795,747,995,1040), 'shoe_side':(1085,755,1448,1025)
}
def main():
    source=Image.open(SOURCE).convert('RGBA')
    assert source.size==(1448,1086)
    output=ROOT/'app/src/main/res/drawable-nodpi'
    manifest={}
    connected=Image.open(SOURCE.parent/'athlete-connected-upper-body.png').convert('RGBA')
    assert connected.size==(1672,941)
    boxes={**BOXES,'body_front':(245,0,725,850),'body_side':(995,0,1385,850)}
    for name,box in boxes.items():
        image=connected if name.startswith('body_') else source
        part=image.crop(box)
        # Use the actual alpha bounds, never replace transparency with a matte.
        bounds=part.getchannel('A').getbbox(); assert bounds
        part=part.crop(bounds)
        part.thumbnail((768,1024) if name.startswith('body_') else (512,512),Image.Resampling.LANCZOS)
        target=output/f'athlete_{name}.webp'
        part.save(target,lossless=True,method=6)
        manifest[name]={'source':'athlete-connected-upper-body.png' if name.startswith('body_') else 'athlete-atlas.png','source_crop':box,'alpha_crop':bounds,'width':part.width,'height':part.height,'bytes':target.stat().st_size}
    (SOURCE.parent/'textures.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
    print(f'Packed {len(manifest)} transparent photographic character textures.')
if __name__=='__main__': main()
