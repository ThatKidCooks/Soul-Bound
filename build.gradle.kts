plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("net.axay:kspigot:1.21.0")
    implementation("org.java-websocket:Java-WebSocket:1.6.0") // Discord bot integration

    // ← Use 5.3.0 (published) instead of 5.4.0
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }

    shadowJar {
        archiveClassifier.set("") // replaces default "-all" so Paper loads it normally
        mergeServiceFiles()       // merge META-INF/services if needed

        // Relocate WebSocket classes to avoid conflicts with other plugins
        relocate("org.java_websocket", "site.thatkid.soulBound.libs.java_websocket")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
