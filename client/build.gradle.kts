import org.jire.swiftfp.build.Dependencies

plugins {
	java
}

dependencies {
	Dependencies.configure(this)
}

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}