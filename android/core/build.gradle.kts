plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

val onnxVersion = "1.20.0"

dependencies {
    // The app supplies onnxruntime-android (same ai.onnxruntime API); tests use the JVM build.
    compileOnly("com.microsoft.onnxruntime:onnxruntime:$onnxVersion")
    testImplementation("com.microsoft.onnxruntime:onnxruntime:$onnxVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
}

tasks.test {
    systemProperty("am.assets", rootProject.file("app/src/main/assets").absolutePath)
    systemProperty(
        "am.reference",
        System.getenv("AM_REFERENCE") ?: rootProject.file("../tools/reference/out/reference.json").absolutePath,
    )
    systemProperty("am.requireParity", System.getenv("AM_REQUIRE_PARITY") ?: "0")
    maxHeapSize = "1g"
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}
