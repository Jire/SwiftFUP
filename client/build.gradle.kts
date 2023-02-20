plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.netty)
    implementation(libs.fastutil)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar").configure {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
