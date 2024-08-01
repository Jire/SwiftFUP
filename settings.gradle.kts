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
        kotlin("jvm") version "2.0.0"
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}

include(
    "common",

    "server",
    "client",

    "packing"
)
