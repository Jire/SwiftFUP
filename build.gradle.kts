subprojects {
	apply(plugin = "java")
	
	group = "org.jire.swiftfup"
	version = "1.0.0"
	
	repositories {
		mavenCentral()
	}
	
	tasks.named<Jar>("jar") {
		archiveBaseName.set("${rootProject.name}-${project.name}")
	}
}