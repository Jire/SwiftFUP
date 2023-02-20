plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.netty)
    implementation(libs.fastutil)
    implementation(libs.zero.allocation.hashing)

    implementation(libs.rs.cache.library)
    implementation(libs.disio)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass.set("org.jire.swiftfup.server.Main")
    applicationDefaultJvmArgs += arrayOf(
        "-XX:+UseZGC",
    )
}

kotlin {
    jvmToolchain(19)
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
