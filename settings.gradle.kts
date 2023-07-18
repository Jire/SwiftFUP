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
        kotlin("jvm") version "1.8.22"
    }
}

include(
    "common",

    "server",
    "client",

    "packing"
)
