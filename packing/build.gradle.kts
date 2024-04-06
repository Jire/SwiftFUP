plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":server"))

    implementation(libs.fastutil)

    implementation(libs.rs.cache.library)
    implementation(libs.disio)

    implementation(libs.netty)

    implementation(libs.gson)

    implementation(libs.tinify)
    implementation(libs.pngtastic)

    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    implementation(files("../lib/cache-1.10.27-SNAPSHOT-jar-with-dependencies.jar"))
}

val defaultJvmArgs = arrayOf(
    //"-XX:+UseZGC",
    "-Xmx4g",
    "-Xms2g",
    "-XX:-OmitStackTraceInFastThrow",
)

val defaultMainClassName = "org.jire.swiftfup.packing.Packer"

fun execTask(
    name: String, mainClassName: String = defaultMainClassName, configure: (JavaExecSpec.() -> Unit)? = null
) = tasks.register(name, JavaExec::class.java) {
    group = ApplicationPlugin.APPLICATION_GROUP

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(mainClassName)
    jvmArgs(*defaultJvmArgs)

    enableAssertions = true
    if (hasProperty("args")) {
        val argsProperty = property("args")
        val argsList = argsProperty as List<*>
        if (argsList.isNotEmpty()) {
            args(argsList)
        }
    }

    configure?.invoke(this)
}

execTask("tarnishPacker", "org.jire.swiftfup.packing.TarnishPacker")
execTask("ethscapePacker", "org.jire.swiftfup.packing.EthscapePacker")
execTask("roatzPacker", "org.jire.swiftfup.packing.roatz.RoatzPacker")
execTask("roatzImageOptimizer", "org.jire.swiftfup.packing.roatz.RoatzImageOptimizer")
execTask("kozaroPacker", "org.jire.swiftfup.packing.KozaroPacker")
execTask("pyronPacker", "org.jire.swiftfup.packing.PyronPacker")
execTask("reasonPacker", "org.jire.swiftfup.packing.reason.ReasonPacker")