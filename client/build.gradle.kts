plugins {
    `java-library`
    `maven-publish`
    signing
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

publishing {
    repositories {
        maven(url = "https://repo.repsy.io/mvn/jire/swiftfup") {
            val repsyUsername = providers.environmentVariable("REPSY_USERNAME")
            val repsyPassword = providers.environmentVariable("REPSY_PASSWORD")
            if (repsyUsername.isPresent && repsyPassword.isPresent) {
                credentials {
                    username = repsyUsername.get()
                    password = repsyPassword.get()
                }
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = rootProject.name
                description = rootProject.description
                url = "https://github.com/Jire/SwiftFUP"
                packaging = "jar"
                developers {
                    developer {
                        id = "Jire"
                        name = "Thomas Nappo"
                        email = "thomasgnappo@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/Jire/SwiftFUP.git"
                    developerConnection = "scm:git:ssh://git@github.com/Jire/SwiftFUP.git"
                    url = "https://github.com/Jire/SwiftFUP"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}