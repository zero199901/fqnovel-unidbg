#!/usr/bin/env python3
"""
工具：触发全本下载流接口，并在服务端将章节写入 Redis 后，调用
现有合并脚本将缓存的章节导出为 TXT。

用法示例：
    python3 tools/fullbook_download_and_merge.py 1630621903720459
    python3 tools/fullbook_download_and_merge.py 书籍ID1 书籍ID2 --batch 50 --max 200
"""

import argparse
import subprocess
import sys
import time
import urllib.parse
import urllib.request


# 本地服务地址（与后端 Spring Boot 的端口保持一致）
BASE = 'http://127.0.0.1:9999'


def stream_fullbook_download(book_id: str, batch_size: int, max_chapters: int | None, save_to_redis: bool, timeout: int) -> None:
    """
    调用 GET 流式接口触发服务端下载，将章节写入 Redis。
    这里消耗响应流（不解析），目的仅是等待下载过程完成。
    """
    params = {
        'bookId': book_id,
        'batchSize': str(batch_size),
        'saveToRedis': 'true' if save_to_redis else 'false',
    }
    if max_chapters is not None:
        params['maxChapters'] = str(max_chapters)

    url = f"{BASE}/api/fullbook/download?" + urllib.parse.urlencode(params)
    req = urllib.request.Request(url)

    start_ts = time.time()
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        # 消费流直到完成；间隔输出点号表示进度保活
        while True:
            chunk = resp.read(8192)
            if not chunk:
                break
            # 适度输出点号，避免刷屏
            if time.time() - start_ts > 0.5:
                print('.', end='', flush=True)
                start_ts = time.time()
    print()  # newline after dots


def run_merge(book_id: str, output_path: str | None, max_chapters: int | None) -> None:
    """
    调用现有脚本 `tools/export_book_cached_merge.py` 合并缓存章节到 TXT。
    - 若提供 output_path，则作为导出路径；
    - 若仅提供 max_chapters 且无 output_path，第二个位置参数将被脚本识别为最大章节数。
    """
    cmd = [sys.executable, 'tools/export_book_cached_merge.py', book_id]
    if output_path:
        cmd.append(output_path)
        if max_chapters is not None:
            cmd.append(str(max_chapters))
    elif max_chapters is not None:
        # When no explicit output path, the merge script allows a numeric second arg as maxChapters
        cmd.append(str(max_chapters))

    print('Running:', ' '.join(cmd))
    subprocess.check_call(cmd)


def main() -> None:
    """
    参数说明（常用）：
    - book_ids：一个或多个书籍 ID；
    - --batch：服务端下载批大小；
    - --max：限制下载/合并的最大章节数；
    - --output：导出 TXT 的显式路径；
    - --no-redis：不将章节保存到 Redis（一般不建议）；
    - --timeout：流式请求的超时时间（秒）。
    """
    parser = argparse.ArgumentParser(description='Trigger fullbook download then merge cached chapters to TXT')
    parser.add_argument('book_ids', nargs='+', help='One or more bookId values')
    parser.add_argument('--batch', type=int, default=30, help='Batch size for server-side download (default: 30)')
    parser.add_argument('--max', dest='max_chapters', type=int, default=None, help='Limit number of chapters to download/merge')
    parser.add_argument('--no-redis', action='store_true', help='Do not save chapters to Redis (server-side)')
    parser.add_argument('--timeout', type=int, default=600, help='HTTP timeout in seconds for stream (default: 600)')
    parser.add_argument('--output', dest='output_path', default=None, help='Explicit output TXT path (optional)')

    args = parser.parse_args()

    for book_id in args.book_ids:
        print(f'==> Downloading bookId={book_id} (batch={args.batch}, max={args.max_chapters})')
        stream_fullbook_download(
            book_id=book_id,
            batch_size=args.batch,
            max_chapters=args.max_chapters,
            save_to_redis=not args.no_redis,
            timeout=args.timeout,
        )
        print(f'==> Merging cached chapters for bookId={book_id}')
        run_merge(book_id, args.output_path, args.max_chapters)


# python3 tools/fullbook_download_and_merge.py 6707112755507235848 6768714041658444811 1624193943574542
if __name__ == '__main__':
    main()


