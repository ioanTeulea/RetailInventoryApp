// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level build file
// Top-level build file
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}