plugins {
    kotlin("jvm") version "2.2.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "site.thatkid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Dmulloy’s repo only hosts up through 5.3.0:
    maven("https://repo.dmulloy2.net/repository/public/") { name = "dmulloy2-repo" }
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc-repo" }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("com.google.code.gson:gson:2.10.1")

    // ← Use 5.3.0 (published) instead of 5.4.0
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

// Configure jar task to exclude all dependencies
tasks.withType<Jar> {
    // Only include your plugin classes
    from(sourceSets.main.get().output)
    // Explicitly exclude everything else
    exclude("META-INF/**")
    exclude("kotlin/**")
    exclude("org/jetbrains/**")
    exclude("com/google/**")
    exclude("io/papermc/**")
    exclude("com/comphenix/**")
    exclude("org/bukkit/**")
    exclude("net/kyori/**")
    // Include only your packages
    include("site/thatkid/**")
    include("plugin.yml")
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    // No special dependencies needed
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
