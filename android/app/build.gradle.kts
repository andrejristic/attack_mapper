plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Version comes from the git tag in CI (AM_VERSION=v1.2.3 -> versionCode 10203, versionName 1.2.3).
val amVersion = System.getenv("AM_VERSION") ?: ""
val versionMatch = Regex("""^v?(\d+)\.(\d+)\.(\d+)(.*)$""").find(amVersion)
val amVersionCode = versionMatch?.destructured?.let { (a, b, c, _) -> a.toInt() * 10000 + b.toInt() * 100 + c.toInt() } ?: 1
val amVersionName = versionMatch?.let { amVersion.removePrefix("v") } ?: "0.0.0-dev"

// Release signing only when the keystore is provided (CI secrets); otherwise debug builds only.
val keystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }

android {
    namespace = "ca.andrej_zeravatech.attackmapper"
    compileSdk = 35

    defaultConfig {
        applicationId = "ca.andrej_zeravatech.attackmapper"
        minSdk = 26
        targetSdk = 35
        versionCode = amVersionCode
        versionName = amVersionName
        ndk {
            // Phones + ARM Chromebooks, and Intel/AMD Chromebooks.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storeType = "pkcs12"
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystorePath != null) signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".test"
            versionNameSuffix = "-test"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        // Release builds must not be blocked by lint on translation/style nits; real errors still fail.
        checkReleaseBuilds = true
        abortOnError = true
        disable += listOf("MissingTranslation")
    }
}

val checkModelAssets by tasks.registering {
    val files = listOf("minilm/model_fp16w.onnx", "minilm/vocab.txt").map { file("src/main/assets/$it") }
    doLast {
        files.filterNot { it.isFile }.takeIf { it.isNotEmpty() }?.let {
            throw GradleException("Missing model assets $it — run tools/fetch_model.sh first")
        }
    }
}
tasks.named("preBuild") { dependsOn(checkModelAssets) }

dependencies {
    implementation(project(":core"))
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.0")

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.core:core-ktx:1.15.0")
}
