import org.jire.swiftfup.build.Dependencies

plugins {
	`java-library`
	`maven-publish`
}

dependencies {
	Dependencies.configure(this, "api")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))