import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.IndraLicenseHeaderPlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.sonatypeSnapshots
import java.io.ByteArrayOutputStream

plugins {
  `java-library`
  id("net.kyori.indra") version "1.3.1"
}

allprojects {
  group = "xyz.jpenilla"
  version = "1.3.1+${latestCommitHash()}-SNAPSHOT"
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
    github("jpenilla", "TabTPS") {
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
      classifier != "javadoc"
        && project.name != rootProject.name
    }
  }
  tasks.withType<Javadoc> {
    onlyIf { false }
  }
}

fun latestCommitHash(): String {
  val byteOut = ByteArrayOutputStream()
  exec {
    commandLine = listOf("git", "rev-parse", "--short", "HEAD")
    standardOutput = byteOut
  }
  return byteOut.toString(Charsets.UTF_8.name()).trim()
}
