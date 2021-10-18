import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "11"
	}
}