#!/usr/bin/env python3
"""フォントサブセット化スクリプト（初回ロードサイズ削減 / Issue #30）。

## 背景
core/designsystem の composeResources/font/ に置かれている 7 本の TTF
(JetBrains Mono x3, Noto Sans JP x2, Zen Kaku Gothic New x2) は、いずれも
Google Fonts 配布のフルグリフセット（Noto Sans JP は特に全 CJK 収録で
1本あたり ~5.7MB）をそのまま同梱している。しかしこのサイトが実際に
描画する日本語はごく少数の静的コンテンツ（自己紹介プロフィール文言）
に限られる。本スクリプトは実際に使われている文字だけを残す形で
各 TTF を **同一ファイル名のまま in-place** サブセット化し、転送量を
大幅に削減する。

## サブセット対象の元フォント
このスクリプトは既存の（サブセット済みかもしれない） TTF を入力として
読み込み、要求文字が足りなければ結果的に「縮むか変わらないか」にしか
ならない（フォントサブセット化は不可逆。一度削った字形は戻せない）。
そのため **再生成は必ずオリジナルのフルグリフフォントから行うこと**。
オリジナルは git 履歴に残っている（本コミットより前の
core/designsystem/src/commonMain/composeResources/font/*.ttf）ので、
`git show <このスクリプト追加前のコミット>:core/designsystem/.../foo.ttf`
で復元するか、Google Fonts から改めてダウンロードしてから実行すること。

## 再生成すべきタイミング
- プロフィール文言など、画面に表示する文字列に新しい漢字/記号を追加したとき
  （例: 将来の Issue #23/#24 で作品ページを API 駆動化し、表示テキストが増える場合）
- フォント自体を新しいバージョンに差し替えたとき

## 実行コマンド
    python3 scripts/subset_fonts.py

リポジトリルート（このファイルの1つ上の階層）から実行しても、
どこから実行してもよい（パスはこのファイルからの相対で解決する）。

## 使用ツール
fonttools の pyftsubset を使う。以下の優先順で利用可能な実行方法を探し、
最初に見つかったものを使う。
  1. `pyftsubset` コマンドが PATH にある
  2. `python3 -m fontTools.subset` が実行できる
  3. `uvx --from fonttools pyftsubset` が実行できる（uv 経由、インストール不要）
いずれも無ければ `pip3 install --user fonttools`（sudo 不要）でインストールしてから
再実行すること。

## keep-set（残す文字）の考え方
1. 固定範囲: ASCII 全域 / Latin-1 Supplement / ひらがな / カタカナ /
   半角・全角形 (Halfwidth and Fullwidth Forms) / CJK 記号・句読点 /
   罫線素片・矢印（Box Drawing / Arrows。IDE 風 UI の JetBrains Mono 用の保険）。
2. 動的範囲: `core/`, `feature/`, `composeApp/` 配下の *.kt を走査し、
   文字列リテラルの中身から実際に登場する文字を丸ごと抽出して追加する。
   CJK 表意文字（漢字）はもちろん、★ や … や — のような固定範囲に
   含まれない記号もここで拾われる（例: GitHubPreviewCard の "★ $stars"）。
   固定範囲に入らない記号を漢字だけに絞ると、実際に使われている記号の
   グリフが欠けて豆腐化するリスクがあるため、抽出した文字は種類を問わず
   全て keep-set に加える。
3. OpenType のレイアウト機能（kern 等）は pyftsubset のデフォルトのまま
   （--layout-features を指定しない = kern を含むデフォルト集合を保持）。

このスクリプトは何度再実行しても安全（idempotent）。既にサブセット済みの
フォントに対して実行した場合、要求文字が全て揃っていれば概ねファイルサイズは
変わらず、削っても問題ない範囲がさらに見つかればより縮む。ただし前述の通り
「削りすぎた」場合の復元はできないため、キャラクタセットを広げる再生成は
必ずオリジナルから行うこと。
"""

from __future__ import annotations

import shutil
import subprocess
import sys
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
REPO_ROOT = SCRIPT_DIR.parent
FONT_DIR = REPO_ROOT / "core" / "designsystem" / "src" / "commonMain" / "composeResources" / "font"
SCAN_DIRS = ["core", "feature", "composeApp"]

FONT_FILES = [
    "jetbrains_mono_regular.ttf",
    "jetbrains_mono_medium.ttf",
    "jetbrains_mono_bold.ttf",
    "noto_sans_jp_medium.ttf",
    "noto_sans_jp_semi_bold.ttf",
    "zen_kaku_gothic_new_regular.ttf",
    "zen_kaku_gothic_new_bold.ttf",
]

# 固定 keep-set（Unicode コードポイント範囲、両端含む）
FIXED_RANGES: list[tuple[int, int]] = [
    (0x0020, 0x007E),  # ASCII（可視文字域）
    (0x00A0, 0x00FF),  # Latin-1 Supplement
    (0x3040, 0x309F),  # ひらがな
    (0x30A0, 0x30FF),  # カタカナ
    (0xFF00, 0xFFEF),  # 半角・全角形
    (0x3000, 0x303F),  # CJK 記号・句読点
    (0x2500, 0x257F),  # 罫線素片 (Box Drawing) — JetBrains Mono 用の保険
    (0x2190, 0x21FF),  # 矢印 (Arrows) — JetBrains Mono 用の保険
]


def find_pyftsubset_runner() -> list[str]:
    """利用可能な pyftsubset 起動コマンドを優先順に探して返す。"""
    if shutil.which("pyftsubset"):
        return ["pyftsubset"]

    try:
        subprocess.run(
            [sys.executable, "-m", "fontTools.subset", "--help"],
            check=True,
            capture_output=True,
        )
        return [sys.executable, "-m", "fontTools.subset"]
    except (subprocess.CalledProcessError, FileNotFoundError):
        pass

    if shutil.which("uvx"):
        try:
            subprocess.run(
                ["uvx", "--from", "fonttools", "pyftsubset", "--help"],
                check=True,
                capture_output=True,
            )
            return ["uvx", "--from", "fonttools", "pyftsubset"]
        except (subprocess.CalledProcessError, FileNotFoundError):
            pass

    print(
        "pyftsubset が見つかりません。以下のいずれかを行ってから再実行してください:\n"
        "  pip3 install --user fonttools\n"
        "  (sudo は使わないこと)",
        file=sys.stderr,
    )
    sys.exit(1)


def extract_string_literals(source: str) -> list[str]:
    """Kotlin ソースから文字列リテラルの中身（テンプレート式は除く）を抜き出す簡易パーサー。

    完全な Kotlin 構文解析ではないが、行コメント・ブロックコメント・
    エスケープシーケンス（\\n, \\uXXXX 等）・文字列テンプレート（$var, ${expr}）を
    考慮し、実際に画面へ描画される文字列リテラルの「素の文字」だけを拾う。
    """
    literals: list[str] = []
    i = 0
    n = len(source)
    buf: list[str] = []

    def flush() -> None:
        if buf:
            literals.append("".join(buf))
            buf.clear()

    while i < n:
        c = source[i]

        # 行コメント
        if c == "/" and i + 1 < n and source[i + 1] == "/":
            i += 2
            while i < n and source[i] != "\n":
                i += 1
            continue

        # ブロックコメント（KDoc含む）
        if c == "/" and i + 1 < n and source[i + 1] == "*":
            i += 2
            while i < n and not (source[i] == "*" and i + 1 < n and source[i + 1] == "/"):
                i += 1
            i += 2
            continue

        # 三重引用符（raw string）
        if source[i:i + 3] == '"""':
            i += 3
            start = i
            while i < n and source[i:i + 3] != '"""':
                i += 1
            literals.append(source[start:i])
            i += 3
            continue

        # 通常の文字列リテラル
        if c == '"':
            i += 1
            depth = 0  # ${ ... } のネスト深さ
            while i < n:
                ch = source[i]
                if depth == 0:
                    if ch == "\\":
                        i += 1
                        if i >= n:
                            break
                        esc = source[i]
                        if esc == "u" and i + 4 < n:
                            hex_digits = source[i + 1:i + 5]
                            try:
                                buf.append(chr(int(hex_digits, 16)))
                            except ValueError:
                                pass
                            i += 5
                            continue
                        mapping = {"n": "\n", "t": "\t", "r": "\r", '"': '"', "\\": "\\", "$": "$", "'": "'"}
                        buf.append(mapping.get(esc, esc))
                        i += 1
                        continue
                    if ch == '"':
                        i += 1
                        break
                    if ch == "$" and i + 1 < n and source[i + 1] == "{":
                        depth = 1
                        i += 2
                        continue
                    if ch == "$" and i + 1 < n and (source[i + 1].isalpha() or source[i + 1] == "_"):
                        # $identifier 形式のテンプレート。式の中身はリテラル文字ではないので飛ばす。
                        i += 1
                        while i < n and (source[i].isalnum() or source[i] == "_"):
                            i += 1
                        continue
                    buf.append(ch)
                    i += 1
                else:
                    # ${ ... } の中（式）。ネストする波括弧と、式中の入れ子文字列を読み飛ばす。
                    if ch == "{":
                        depth += 1
                        i += 1
                    elif ch == "}":
                        depth -= 1
                        i += 1
                    elif ch == '"':
                        i += 1
                        while i < n and source[i] != '"':
                            i += 2 if source[i] == "\\" else 1
                        i += 1
                    else:
                        i += 1
            flush()
            continue

        i += 1

    flush()
    return literals


def scan_rendered_characters() -> set[str]:
    """core/, feature/, composeApp/ 配下の *.kt から実際に描画されうる文字集合を集める。"""
    chars: set[str] = set()
    for base in SCAN_DIRS:
        base_dir = REPO_ROOT / base
        if not base_dir.exists():
            continue
        for path in sorted(base_dir.rglob("*.kt")):
            if "/build/" in str(path):
                continue
            source = path.read_text(encoding="utf-8")
            for literal in extract_string_literals(source):
                chars.update(literal)
    # 制御文字（改行・タブ等）はグリフを持たないので除外
    return {c for c in chars if c.isprintable()}


def codepoints_to_ranges(codepoints: set[int]) -> str:
    """コードポイント集合を pyftsubset --unicodes 向けのコンパクトな hex レンジ文字列に変換する。"""
    points = sorted(codepoints)
    ranges: list[tuple[int, int]] = []
    start = prev = points[0]
    for cp in points[1:]:
        if cp == prev + 1:
            prev = cp
            continue
        ranges.append((start, prev))
        start = prev = cp
    ranges.append((start, prev))

    parts = []
    for a, b in ranges:
        parts.append(f"{a:04X}" if a == b else f"{a:04X}-{b:04X}")
    return ",".join(parts)


def build_keep_set() -> set[int]:
    keep: set[int] = set()
    for start, end in FIXED_RANGES:
        keep.update(range(start, end + 1))

    extracted = scan_rendered_characters()
    keep.update(ord(c) for c in extracted)

    return keep


def subset_font(runner: list[str], font_path: Path, unicodes: str) -> None:
    tmp_path = font_path.with_suffix(font_path.suffix + ".subset.tmp")
    cmd = runner + [
        str(font_path),
        f"--unicodes={unicodes}",
        f"--output-file={tmp_path}",
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(result.stdout)
        print(result.stderr, file=sys.stderr)
        raise SystemExit(f"pyftsubset failed for {font_path.name}")
    tmp_path.replace(font_path)


def format_size(num_bytes: int) -> str:
    return f"{num_bytes / 1024:.1f} KiB"


def main() -> None:
    runner = find_pyftsubset_runner()
    print(f"pyftsubset 起動コマンド: {' '.join(runner)}")

    print("Kotlin ソースを走査して描画文字を抽出中...")
    keep_codepoints = build_keep_set()
    unicodes = codepoints_to_ranges(keep_codepoints)
    print(f"keep-set: {len(keep_codepoints)} 文字 ({len(unicodes.split(','))} レンジ)")

    rows: list[tuple[str, int, int]] = []
    for filename in FONT_FILES:
        font_path = FONT_DIR / filename
        if not font_path.exists():
            print(f"警告: {font_path} が存在しません。スキップします。", file=sys.stderr)
            continue
        before = font_path.stat().st_size
        subset_font(runner, font_path, unicodes)
        after = font_path.stat().st_size
        rows.append((filename, before, after))

    print()
    header = f"{'file':<32} {'before':>12} {'after':>12} {'reduction':>10}"
    print(header)
    print("-" * len(header))
    total_before = total_after = 0
    for filename, before, after in rows:
        total_before += before
        total_after += after
        reduction = 100 * (1 - after / before) if before else 0
        print(f"{filename:<32} {format_size(before):>12} {format_size(after):>12} {reduction:>9.1f}%")
    print("-" * len(header))
    total_reduction = 100 * (1 - total_after / total_before) if total_before else 0
    print(f"{'TOTAL':<32} {format_size(total_before):>12} {format_size(total_after):>12} {total_reduction:>9.1f}%")


if __name__ == "__main__":
    main()
