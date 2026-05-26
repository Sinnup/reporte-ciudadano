plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
}

// Mirrors the jobs in .github/workflows/kmp.yml so CI can be reproduced locally.
// Usage: ./gradlew ciLocal
tasks.register("ciLocal") {
    group = "verification"
    description = "Runs the same compilation and test tasks as the GitHub Actions KMP CI pipeline."

    dependsOn(
        ":androidApp:compileDebugKotlin",          // compile-android
        ":shared:compileKotlinIosSimulatorArm64",  // compile-ios
        ":shared:compileKotlinJs",                 // compile-js
        ":shared:compileKotlinWasmJs",             // compile-wasmjs
        ":shared:testAndroidHostTest"              // test-jvm
    )
}