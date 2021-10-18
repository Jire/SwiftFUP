import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jire.swiftfup.build.Dependencies

plugins {
	kotlin("jvm")
	application
	id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "org.jire.swiftfup"
version = "0.1.0"

application {
	mainClass.set("org.jire.swiftfup.server.Main")
}

dependencies {
	Dependencies.configure(this)
	implementation("net.openhft:zero-allocation-hashing:0.15")
	
	implementation(kotlin("stdlib"))
	implementation("com.displee:rs-cache-library:6.8.1")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "11"
}