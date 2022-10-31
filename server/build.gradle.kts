import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jire.swiftfup.build.Dependencies

plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    Dependencies.configure(this)
    implementation("net.openhft:zero-allocation-hashing:0.16")

    implementation("com.displee:rs-cache-library:6.8.1")
    implementation("com.displee:disio:2.2")
}

application {
    mainClass.set("org.jire.swiftfup.server.Main")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
