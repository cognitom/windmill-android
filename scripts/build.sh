#! /usr/bin/env bash
set -euo pipefail

# gradle は compose.agent.yaml の gradle サービスのものを呼ぶ
cd "$(dirname "$0")/.."

gradle assembleDebug
# APK はコンテナの中で app/build/ にできる。リポジトリごとマウントしているので、
# ホスト側から拾える
mkdir -p output
cp app/build/outputs/apk/debug/app-debug.apk output/windmill-android-debug.apk
