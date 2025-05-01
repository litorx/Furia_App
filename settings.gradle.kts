pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {

        id("com.android.application")       version "8.0.0" apply false
        id("org.jetbrains.kotlin.android") version "1.9.0" apply false
        id("org.jetbrains.kotlin.kapt")    version "1.9.0" apply false
        id("dagger.hilt.android.plugin")   version "2.44"   apply false
        id("com.google.protobuf")         version "0.9.2" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FuriaFanApp"
include(":app")
