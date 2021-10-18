subprojects {
	apply(plugin = "java")
	
	group = "org.jire.swiftfup"
	version = "0.3.2"
	
	repositories {
		mavenCentral()
	}
	
	tasks.named<Jar>("jar") {
		archiveBaseName.set("${rootProject.name}-${project.name}")
	}
}