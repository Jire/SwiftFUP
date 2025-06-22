rootProject.name = "SwiftFUP"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.runelite.net/")
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.21"
        id("com.gradleup.shadow") version "8.3.6"
    }
}

include(
    "common",

    "server",
    "client",

    "packing"
)
