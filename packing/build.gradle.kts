plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":common"))

    implementation(libs.fastutil)

    implementation(libs.rs.cache.library)
    implementation(libs.disio)

    implementation(libs.netty)

    implementation(libs.gson)

    implementation(libs.runelite.cache)
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
execTask("kozaroPacker", "org.jire.swiftfup.packing.KozaroPacker")
execTask("pyronPacker", "org.jire.swiftfup.packing.PyronPacker")