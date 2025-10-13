#!/usr/bin/env python3
import json
import os
import re
import html
import sys
import urllib.parse
import urllib.request
from typing import List

BASE = 'http://127.0.0.1:9999'

def http_get(url: str) -> str:
    with urllib.request.urlopen(url) as r:
        return r.read().decode('utf-8')

def html_to_text(raw: str) -> str:
    if not raw:
        return ''
    raw = raw.replace('<br>', '\n').replace('<br/>', '\n').replace('<br />', '\n')
    raw = re.sub(r'</p\s*>', '\n', raw, flags=re.I)
    text = re.sub(r'<[^>]+>', '', raw)
    text = html.unescape(text)
    text = re.sub(r'\n{3,}', '\n\n', text).strip()
    return text

def fetch_book_info(book_id: str) -> dict:
    url = f"{BASE}/api/cache/book/{book_id}/info"
    try:
        obj = json.loads(http_get(url))
        return obj
    except Exception:
        return {}

def fetch_chapter_ids(book_id: str) -> List[str]:
    key = f"book:{book_id}:chapters"
    url = f"{BASE}/api/cache/value?" + urllib.parse.urlencode({'key': key})
    wrap = json.loads(http_get(url))
    val = wrap.get('value') or '[]'
    try:
        ids = json.loads(val)
        return [str(x) for x in ids] if isinstance(ids, list) else []
    except Exception:
        return []

def fetch_chapter_from_cache(book_id: str, chapter_id: str) -> dict:
    key = f"novel:chapter:{book_id}:{chapter_id}"
    url = f"{BASE}/api/cache/value?" + urllib.parse.urlencode({'key': key})
    wrap = json.loads(http_get(url))
    val = wrap.get('value') or ''
    return json.loads(val) if val else {}

def normalize_title(n: int, title: str) -> str:
    if not title:
        return f'第{n}章'
    return re.sub(r'^第\s*'+str(n)+r'\s*章\s*', '', title).strip()

def main():
    # CLI:
    # 1) python3 tools/export_book_cached_merge.py <bookId>
    #    -> outputs to results/novels/<bookName or bookId>.txt
    # 2) python3 tools/export_book_cached_merge.py <bookId> <outputPath> [maxChapters]
    if len(sys.argv) < 2:
        print('Usage: python3 tools/export_book_cached_merge.py <bookId> [outputPath] [maxChapters]')
        sys.exit(1)
    book_id = sys.argv[1]

    info = fetch_book_info(book_id)
    book_name = info.get('bookName') or info.get('book_name') or ''
    author = info.get('author') or ''
    abstract = info.get('description') or info.get('abstract') or ''

    # Derive default output path when not provided
    if len(sys.argv) >= 3 and not sys.argv[2].isdigit():
        output_path = sys.argv[2]
        max_ch = int(sys.argv[3]) if len(sys.argv) >= 4 else None
    else:
        # sanitize book_name for filename
        safe_name = (book_name or book_id)
        # Replace path-unfriendly chars
        safe_name = re.sub(r'[\\/\n\r\t:*?"<>|]', '_', safe_name)
        output_dir = 'results/novels'
        output_path = os.path.join(output_dir, f'{safe_name}.txt')
        max_ch = int(sys.argv[2]) if len(sys.argv) >= 3 and sys.argv[2].isdigit() else None

    ids = fetch_chapter_ids(book_id)
    if not ids:
        print('No chapter ids found in Redis', file=sys.stderr)
        sys.exit(2)
    total = len(ids)
    if max_ch is not None:
        ids = ids[:max_ch]

    os.makedirs(os.path.dirname(output_path) or '.', exist_ok=True)
    with open(output_path, 'w', encoding='utf-8') as f:
        # header
        f.write(f'小说名：{book_name or book_id}\n')
        if author:
            f.write(f'作者：{author}\n')
        if abstract:
            f.write(f'内容简介：{abstract}\n')
        f.write('\n')
        # chapters
        for i, cid in enumerate(ids, start=1):
            ch = fetch_chapter_from_cache(book_id, cid)
            title = ch.get('title') or ch.get('chapterName') or ''
            title = normalize_title(i, title)
            text = ch.get('txtContent') or ''
            if not text:
                text = html_to_text(ch.get('rawContent') or '')
            if not text.endswith('\n'):
                text += '\n'
            f.write(f'第{i}章 {title}\n\n')
            f.write(text)
            f.write('\n')
    print(f'Merged {len(ids)}/{total} chapters -> {output_path}')

if __name__ == '__main__':
    main()
