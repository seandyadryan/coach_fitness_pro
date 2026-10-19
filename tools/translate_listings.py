"""Create machine-translated listing files for every documented Google Play locale.

Only public marketing copy is sent to Google Translate. This is a convenience
client for its public translation endpoint, not the Play Console import API.
Existing outputs are cached. Native-speaker review is required before release.
"""
from pathlib import Path
import concurrent.futures, csv, json, time, urllib.parse, urllib.request

ROOT=Path(__file__).resolve().parents[1]
LOCALES='af sq am ar hy-AM az-AZ bn-BD eu-ES be bg my-MM ca zh-HK zh-CN zh-TW hr cs-CZ da-DK nl-NL en-AU en-CA en-US en-GB en-IN en-SG en-ZA et fil fi-FI fr-CA fr-FR gl-ES ka-GE de-DE el-GR gu iw-IL hi-IN hu-HU is-IS id it-IT ja-JP kn-IN kk km-KH ko-KR ky-KG lo-LA lv lt mk-MK ms-MY ms ml-IN mr-IN mn-MN ne-NP no-NO fa fa-AE fa-AF fa-IR pl-PL pt-BR pt-PT pa ro rm ru-RU sr si-LK sk sl es-419 es-ES es-US sw sv-SE ta-IN te-IN th tr-TR uk ur vi'.split()
OUT=ROOT/'store'/'translations'; OUT.mkdir(parents=True,exist_ok=True)
CACHE=ROOT/'.tools'/'translations'; CACHE.mkdir(parents=True,exist_ok=True)
FULL=(ROOT/'store/full-description.txt').read_text(encoding='utf-8').strip()
SHORT=(ROOT/'store/short-description.txt').read_text(encoding='utf-8').strip()
def translate(text, language):
    key=language+'-'+__import__('hashlib').sha256(text.encode()).hexdigest()[:16]
    path=CACHE/(key+'.txt')
    if path.exists(): return path.read_text(encoding='utf-8')
    url='https://translate.googleapis.com/translate_a/single?'+urllib.parse.urlencode(dict(client='gtx',sl='en',tl=language,dt='t',q=text))
    for attempt in range(5):
        try:
            req=urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0'})
            data=json.load(urllib.request.urlopen(req,timeout=45))
            translated=''.join(part[0] for part in data[0] if part[0])
            if not translated.strip(): raise ValueError('Empty translation')
            path.write_text(translated,encoding='utf-8')
            return translated
        except Exception:
            if attempt==4: raise
            time.sleep(2**attempt)
def one(locale):
    override=ROOT/'store'/'overrides'/(locale+'.json')
    if override.exists():
        entry=json.loads(override.read_text(encoding='utf-8'))
        assert len(entry['short_description'])<=80 and len(entry['full_description'])<=4000
        (OUT/(locale+'.json')).write_text(json.dumps(entry,ensure_ascii=False,indent=2),encoding='utf-8')
        metadata=ROOT/'fastlane'/'metadata'/'android'/locale; metadata.mkdir(parents=True,exist_ok=True)
        for filename,key in [('title.txt','title'),('short_description.txt','short_description'),('full_description.txt','full_description')]: (metadata/filename).write_text(entry[key]+'\n',encoding='utf-8')
        return entry
    language={'iw':'he','fil':'tl','no':'no','zh-HK':'zh-TW','zh-CN':'zh-CN','zh-TW':'zh-TW','pt-BR':'pt','pt-PT':'pt','es-419':'es'}.get(locale, locale.split('-')[0])
    if locale.startswith('en-'): short,full=SHORT,FULL
    else:
        short=translate(SHORT,language)
        for source in ['Offline workouts, session timer and progress tracking.','Offline workouts and progress tracking.','Offline fitness coach.']:
            if len(short)<=80: break
            short=translate(source,language)
        full=translate(FULL,language)
    assert len(short)<=80,(locale,len(short))
    assert len(full)<=4000,(locale,len(full))
    entry=dict(locale=locale,title='Coach Fitness PRO',short_description=short,full_description=full,review_status='machine_translation_requires_review' if not locale.startswith('en-') else 'source')
    (OUT/(locale+'.json')).write_text(json.dumps(entry,ensure_ascii=False,indent=2),encoding='utf-8')
    metadata=ROOT/'fastlane'/'metadata'/'android'/locale; metadata.mkdir(parents=True,exist_ok=True)
    for filename,content in [('title.txt',entry['title']),('short_description.txt',short),('full_description.txt',full)]: (metadata/filename).write_text(content+'\n',encoding='utf-8')
    return entry
def main():
    entries=[]; errors=[]
    # Duplicate dialects share the base translation. Complete each base language
    # serially within a task to avoid racing cache writes.
    with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
        jobs={pool.submit(one,locale):locale for locale in LOCALES}
        for job in concurrent.futures.as_completed(jobs):
            try:
                item=job.result(); entries.append(item)
                print(f"{item['locale']}: OK ({len(item['short_description'])}/{len(item['full_description'])})",flush=True)
            except Exception as e: errors.append((jobs[job],str(e))); print('FAILED',jobs[job],str(e),flush=True)
    entries.sort(key=lambda row:LOCALES.index(row['locale']))
    (ROOT/'store'/'listing-translations.json').write_text(json.dumps(entries,ensure_ascii=False,indent=2),encoding='utf-8')
    with (ROOT/'store'/'listing-import.csv').open('w',encoding='utf-8-sig',newline='') as f:
        writer=csv.DictWriter(f,fieldnames=['locale','title','short_description','full_description','review_status'],extrasaction='ignore'); writer.writeheader(); writer.writerows(entries)
    report=dict(source='https://support.google.com/googleplay/android-developer/answer/9844778?hl=en',requested=len(LOCALES),completed=len(entries),errors=errors,translation_engine='Google Translate machine translation; AI-authored Romansh override',notes='Regional dialect variants share base-language translations. CSV is an interchange file, not a guaranteed Play Console UI import schema. Review before publishing.')
    (ROOT/'store'/'translation-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    if errors: raise SystemExit(f'{len(errors)} translations failed; rerun to use cache and retry.')
if __name__=='__main__': main()
