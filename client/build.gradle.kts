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
    runtimeOnly(libs.slf4j.simple)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
