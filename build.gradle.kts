plugins {
    id("com.android.application") version libs.versions.agp.get() apply false
    id("com.android.library") version libs.versions.agp.get() apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false // Ensure your Kotlin version is correct

}

buildscript {
    dependencies {
        // You might not strictly need this anymore if the plugins block with alias works correctly.
        // However, keeping it for now can sometimes resolve issues.
        classpath("com.google.gms:google-services:4.4.1")
    }
}

allprojects {
    // Repositories are managed in settings.gradle.kts
}