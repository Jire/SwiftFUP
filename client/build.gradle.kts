import org.jire.swiftfup.build.Dependencies

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    Dependencies.configure(this, "api")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
