// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    val daggerVersion = "2.43"  // Specify the latest version of Dagger here

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.dagger:dagger-compiler:$daggerVersion")  // Add Dagger Compiler
    }
}