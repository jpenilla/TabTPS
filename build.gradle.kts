import java.io.ByteArrayOutputStream

plugins {
    `java-library`
}

allprojects {
    group = "xyz.jpenilla"
    version = "1.2.3+${latestCommitHash()}-SNAPSHOT"
    description = "Monitor your server's performance in the tab menu"
}

subprojects {
    apply<JavaLibraryPlugin>()

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.jpenilla.xyz/snapshots/")
        maven("https://repo.codemc.org/repository/maven-public")
        maven("https://repo.aikar.co/content/groups/aikar/")
        maven("https://jitpack.io")
    }
}

ext["nmsRevisions"] = mapOf(
        Pair("v1_8_R3", "1.8.8-R0.1-SNAPSHOT"),
        Pair("v1_9_R2", "1.9.4-R0.1-SNAPSHOT"),
        Pair("v1_10_R1", "1.10.2-R0.1-SNAPSHOT"),
        Pair("v1_11_R1", "1.11.2-R0.1-SNAPSHOT"),
        Pair("v1_12_R1", "1.12.2-R0.1-SNAPSHOT"),
        Pair("v1_13_R2", "1.13.2-R0.1-SNAPSHOT"),
        Pair("v1_14_R1", "1.14.4-R0.1-SNAPSHOT"),
        Pair("v1_15_R1", "1.15.2-R0.1-SNAPSHOT"),
        Pair("v1_16_R3", "1.16.4-R0.1-SNAPSHOT")
)

fun latestCommitHash(): String {
    val byteOut = ByteArrayOutputStream()
    exec {
        commandLine = listOf("git", "rev-parse", "--short", "HEAD")
        standardOutput = byteOut
    }
    return byteOut.toString(Charsets.UTF_8.name()).trim()
}