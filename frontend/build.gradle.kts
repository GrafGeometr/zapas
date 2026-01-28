// build.gradle.kts (project level)
plugins {
    id("com.android.application") version "8.3.1" apply false  // Обновлено
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false  // Обновлено
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.23" apply false  // Обновлено
}

buildscript {  // Добавьте этот блок
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}