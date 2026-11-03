import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// release の署名に使う環境変数。CI では Secrets から、手元ではシェルから渡す
val releaseSigningEnvNames = listOf(
    "WINDMILL_KEYSTORE",
    "WINDMILL_KEYSTORE_PASSWORD",
    "WINDMILL_KEY_ALIAS",
    "WINDMILL_KEY_PASSWORD",
)

// 空のものは無いものとして扱う
val releaseSigningEnv: Map<String, String> = releaseSigningEnvNames.mapNotNull { name ->
    providers.environmentVariable(name).orNull?.takeIf { it.isNotEmpty() }?.let { name to it }
}.toMap()

android {
    namespace = "io.github.cognitom.windmill"
    compileSdk = 35
    // Dockerfile の BUILD_TOOLS_VERSION と揃える
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "io.github.cognitom.windmill"
        minSdk = 30
        targetSdk = 35
        // Release ワークフローはタグ (vX.Y.Z) から -PversionName / -PversionCode を渡す
        // (計算は scripts/build-release.sh)。渡されない手元のビルドは今の値のまま
        versionCode = (findProperty("versionCode") as String?)?.toInt() ?: 1
        versionName = (findProperty("versionName") as String?) ?: "0.1.0"
    }

    signingConfigs {
        create("release") {
            // 鍵ストアとパスワードはリポジトリに置かず、環境変数で受け取る。
            // gradle はコンテナの中で動くので、鍵ストアのパスはリポジトリのルートからの
            // 相対で渡す (ホストの絶対パスはコンテナから見えない)
            releaseSigningEnv["WINDMILL_KEYSTORE"]?.let { storeFile = rootProject.file(it) }
            storePassword = releaseSigningEnv["WINDMILL_KEYSTORE_PASSWORD"]
            keyAlias = releaseSigningEnv["WINDMILL_KEY_ALIAS"]
            keyPassword = releaseSigningEnv["WINDMILL_KEY_PASSWORD"]
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// 署名の値が欠けていると、AGP は黙って未署名の APK (app-release-unsigned.apk) を作る。
// 配れない APK ができて気づかないのを避けるため、release のビルドはここで止める。
// debug のビルドや lintDebug は release の値を要らないので、設定時ではなく実行時に見る
val checkReleaseSigning by tasks.registering {
    val missing = releaseSigningEnvNames.filterNot { it in releaseSigningEnv }
    doLast {
        if (missing.isNotEmpty()) {
            throw GradleException(
                "release の署名に要る環境変数がありません: ${missing.joinToString(", ")}\n" +
                    "WINDMILL_KEYSTORE はリポジトリのルートからの相対パスで渡してください " +
                    "(CLAUDE.md の「リリース」を参照)",
            )
        }
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(checkReleaseSigning)
}
