subprojects {
	apply(plugin = "java")
	
	group = "org.jire.swiftfup"
	version = "1.1.1"
	
	repositories {
		mavenCentral()
	}
	
	tasks.named<Jar>("jar") {
		archiveBaseName.set("${rootProject.name}-${project.name}")
	}
}