import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.IndraLicenseHeaderPlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.sonatypeSnapshots
import java.io.ByteArrayOutputStream

plugins {
  `java-library`
  id("net.kyori.indra") version "1.2.1"
}

allprojects {
  group = "xyz.jpenilla"
  version = "1.3.0+${latestCommitHash()}-SNAPSHOT"
  description = "Monitor your server's performance in real time"
}

subprojects {
  apply<JavaLibraryPlugin>()
  apply<IndraPlugin>()
  apply<IndraCheckstylePlugin>()
  apply<IndraLicenseHeaderPlugin>()

  repositories {
    mavenLocal()
    mavenCentral()
    sonatypeSnapshots()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://jitpack.io")
  }

  indra {
    javaVersions.target.set(8)
    github("jmanpenilla", "TabTPS") {
      issues = true
    }
    mitLicense()
  }

  tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:-processing")
  }
}

allprojects {
  tasks.withType<Jar> {
    onlyIf {
      val classifier = archiveClassifier.get()
      classifier != "sources"
        && classifier != "javadoc"
        && project.name != rootProject.name
    }
  }
  tasks.withType<Javadoc> {
    onlyIf { false }
  }
}

ext["nmsRevisions"] = mapOf(
  "v1_8_R3" to "1.8.8-R0.1-SNAPSHOT",
  "v1_9_R2" to "1.9.4-R0.1-SNAPSHOT",
  "v1_10_R1" to "1.10.2-R0.1-SNAPSHOT",
  "v1_11_R1" to "1.11.2-R0.1-SNAPSHOT",
  "v1_12_R1" to "1.12.2-R0.1-SNAPSHOT",
  "v1_13_R2" to "1.13.2-R0.1-SNAPSHOT",
  "v1_14_R1" to "1.14.4-R0.1-SNAPSHOT",
  "v1_15_R1" to "1.15.2-R0.1-SNAPSHOT",
  "v1_16_R3" to "1.16.4-R0.1-SNAPSHOT"
)

fun latestCommitHash(): String {
  val byteOut = ByteArrayOutputStream()
  exec {
    commandLine = listOf("git", "rev-parse", "--short", "HEAD")
    standardOutput = byteOut
  }
  return byteOut.toString(Charsets.UTF_8.name()).trim()
}
