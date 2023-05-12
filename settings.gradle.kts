rootProject.name = "SwiftFUP"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.21"
    }
}

include(
    "server",
    "client"
)
