FROM eclipse-temurin:17-jdk-jammy

ARG CMDLINE_TOOLS_VERSION=11076708
ARG ANDROID_PLATFORM=android-35
# app/build.gradle.kts の buildToolsVersion と揃える
ARG BUILD_TOOLS_VERSION=35.0.0
ARG GRADLE_VERSION=8.11.1

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:/opt/gradle/bin:$ANDROID_HOME/cmdline-tools/latest/bin

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl unzip \
 && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" \
 && mkdir -p "$ANDROID_HOME/cmdline-tools" \
 && unzip -q /tmp/tools.zip -d "$ANDROID_HOME/cmdline-tools" \
 && mv "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest" \
 && rm /tmp/tools.zip \
 && yes | sdkmanager --licenses > /dev/null \
 && sdkmanager platform-tools "platforms;${ANDROID_PLATFORM}" "build-tools;${BUILD_TOOLS_VERSION}"

RUN curl -fsSL -o /tmp/gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
 && unzip -q /tmp/gradle.zip -d /opt \
 && mv "/opt/gradle-${GRADLE_VERSION}" /opt/gradle \
 && rm /tmp/gradle.zip

# イメージを作る間の sdkmanager に root の持ち物として作らせないよう、最後に置く。
# Gradle のキャッシュは compose.agent.yaml のボリュームに置く。shim は rootful の
# docker では root 以外で入ってくるので、$HOME ではなく固定の場所を指す
ENV GRADLE_USER_HOME=/gradle-cache
# デバッグ用の署名鍵は ANDROID_USER_HOME に作られる。キャッシュと同じボリュームに
# 置き、環境を作り直しても同じ鍵で署名されるようにする
# (鍵が変わると上書きインストールできず、ユーザー補助の許可も外れる)
ENV ANDROID_USER_HOME=/gradle-cache/android
