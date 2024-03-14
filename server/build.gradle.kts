plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":common"))

    implementation(libs.netty)
    implementation(libs.fastutil)
    implementation(libs.zero.allocation.hashing)

    implementation(libs.rs.cache.library)
    implementation(libs.disio)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.slf4j.simple)
}

application {
    mainClass.set("org.jire.swiftfup.server.Main")
    applicationDefaultJvmArgs += arrayOf(
        "-XX:-OmitStackTraceInFastThrow",

        "--enable-preview",
        "-noverify",

        "-XX:+UseZGC",

        "-Xms1g",

        "-XX:CompileThreshold=1500",
        "-XX:+UseStringDeduplication",
        "-XX:AutoBoxCacheMax=65535"
    )
}

tasks.named<Jar>("jar").configure {
    archiveBaseName.set("${rootProject.name}-${project.name}")
}
