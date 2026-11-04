#! /usr/bin/env bash
set -euo pipefail

# 署名した release の APK を output/windmill-android-vX.Y.Z.apk に出す。
# gradle は compose.agent.yaml の gradle サービスのものを呼ぶ。署名の値は環境変数で渡す
# (WINDMILL_KEYSTORE はリポジトリのルートからの相対パス。CLAUDE.md の「リリース」を参照)
cd "$(dirname "$0")/.."

usage() {
  cat <<'EOS' 1>&2
使い方: scripts/build-release.sh vX.Y.Z

  vX.Y.Z  付けるバージョン。Release ワークフローはタグ名をそのまま渡す
EOS
}

if [ $# -ne 1 ]; then
  usage
  exit 1
fi

version=$1
if ! [[ "$version" =~ ^v([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "🚨  バージョンは vX.Y.Z の形で渡してください: ${version}" 1>&2
  exit 1
fi
major=$((10#${BASH_REMATCH[1]}))
minor=$((10#${BASH_REMATCH[2]}))
patch=$((10#${BASH_REMATCH[3]}))

# versionCode は X * 10000 + Y * 100 + Z。Y や Z が 100 に届くと次の桁と重なり、
# 古い版より小さい versionCode ができて上書きインストールできなくなるので弾く
if [ "$minor" -ge 100 ] || [ "$patch" -ge 100 ]; then
  echo "🚨  マイナー版とパッチ版は 99 までです: ${version}" 1>&2
  exit 1
fi
version_name="${major}.${minor}.${patch}"
version_code=$((major * 10000 + minor * 100 + patch))

gradle assembleRelease -PversionName="$version_name" -PversionCode="$version_code"
# APK はコンテナの中で app/build/ にできる。リポジトリごとマウントしているので、
# ホスト側から拾える
mkdir -p output
cp app/build/outputs/apk/release/app-release.apk "output/windmill-android-${version}.apk"
echo "📦  output/windmill-android-${version}.apk (versionName ${version_name}, versionCode ${version_code})"
