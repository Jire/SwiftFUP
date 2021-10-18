subprojects {
	apply(plugin = "java")
	
	group = "org.jire.swiftfp"
	version = "0.2.0"
	
	repositories {
		mavenCentral()
	}
	
	tasks.named<Jar>("jar") {
		archiveBaseName.set("${rootProject.name}-${project.name}")
	}
}