// build.gradle.kts (project level)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

buildscript {
    dependencies {
        // Hilt classpath is still needed for some configurations, but we've updated its version in TOML
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57.2")
    }
}