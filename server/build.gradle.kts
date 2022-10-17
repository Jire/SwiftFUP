import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jire.swiftfup.build.Dependencies

plugins {
	kotlin("jvm")
	application
	id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
	mainClass.set("org.jire.swiftfup.server.Main")
}

dependencies {
	Dependencies.configure(this)
	implementation("net.openhft:zero-allocation-hashing:0.16")
	
	implementation(kotlin("stdlib"))
	implementation("com.displee:rs-cache-library:6.8.1")
	implementation("com.displee:disio:2.2")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(11))

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "11"
}