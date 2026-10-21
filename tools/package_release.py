"""Create a Play Store handoff ZIP from verified CI binaries and public assets.

The allowlist deliberately excludes JKS files, ignored signing properties,
Gradle caches, emulator images and repository metadata.
"""
from pathlib import Path
import argparse,hashlib,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser()
parser.add_argument('--version',default='1.0.0')
parser.add_argument('--version-code',type=int,default=3)
parser.add_argument('--source-commit',default='941bf1eee922490e844ec8582d95de00cf90998a')
parser.add_argument('--build-run',default='https://github.com/seandyadryan/coach_fitness_pro/actions/runs/39214476702')
parser.add_argument('--directory',type=Path,default=ROOT/'artifacts/release')
parser.add_argument('--require-movements',action='store_true')
args=parser.parse_args()
OUT=args.directory; OUT.mkdir(parents=True,exist_ok=True)
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest()
files=[OUT/f'CoachFitnessPRO-v{args.version}.apk',OUT/f'CoachFitnessPRO-v{args.version}.aab',OUT/'mapping.txt']
assert all(f.exists() for f in files)
validation=json.loads((ROOT/'store/asset-validation.json').read_text())
assert validation['status']=='PASS'
assert 20<=validation['video_seconds']<=32
if args.require_movements:
    assert validation['movement_animation_count']==21
    assert validation['native_movement_video']['seconds']>=5
    movement_qa=json.loads((ROOT/'store/movement-qa.json').read_text())
    assert all(line.startswith('PASS') for line in movement_qa['checks'])
    if 'version_name' in movement_qa:
        assert movement_qa['version_name']==args.version
        assert movement_qa['version_code']==args.version_code
manifest=dict(app='Coach Fitness PRO',developer='PT Deploy Dulu Pulang Nanti',application_id='com.deploydulupulangnanti.coachfitness',version_name=args.version,version_code=args.version_code,source_commit=args.source_commit,build_run=args.build_run,privacy_url='https://seandyadryan.github.io/coach_fitness_pro/',listing_locales=86,screenshots=validation['screenshot_count'],movement_animations=validation.get('movement_animation_count',0),signing_key='External JKS, not included in this archive',youtube_url=None,files=[dict(name=f.name,bytes=f.stat().st_size,sha256=digest(f)) for f in files])
(OUT/'release-manifest.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
archive=OUT/f'CoachFitnessPRO-PlayStore-Package-v{args.version}.zip'
with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as bundle:
    for directory in ['store','docs','fastlane/metadata/android']:
        for path in sorted((ROOT/directory).rglob('*')):
            if path.is_file():
                assert path.suffix.lower() not in ['.jks','.keystore','.pem']
                bundle.write(path,path.relative_to(ROOT).as_posix())
    for path in files: bundle.write(path,'android/'+path.name)
    bundle.write(OUT/'release-manifest.json','release-manifest.json')
    bundle.write(ROOT/'README.md','README.md')
checksums=files+[archive,OUT/'release-manifest.json']
if args.require_movements:
    animation_archive=OUT/f'CoachFitnessPRO-Movement-Animations-v{args.version}.zip'
    with zipfile.ZipFile(animation_archive,'w',zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted((ROOT/'store/animations').rglob('*')):
            if path.is_file(): bundle.write(path,path.relative_to(ROOT/'store').as_posix())
        for name in ['movement-qa.json','ANIMATION-RELEASE.md']:
            bundle.write(ROOT/'store'/name,name)
    checksums.append(animation_archive)
(OUT/'SHA256SUMS.txt').write_text('\n'.join(f'{digest(path)}  {path.name}' for path in checksums)+'\n',encoding='utf-8')
print(json.dumps(dict(archive=str(archive),bytes=archive.stat().st_size,sha256=digest(archive)),indent=2))
