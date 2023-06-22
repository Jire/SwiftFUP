plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":common"))

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
}

tasks.named<Jar>("jar").configure {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
