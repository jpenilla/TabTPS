import net.kyori.indra.IndraCheckstylePlugin
import net.kyori.indra.IndraLicenseHeaderPlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.sonatypeSnapshots
import java.io.ByteArrayOutputStream

plugins {
  `java-library`
  id("net.kyori.indra") version "1.3.1"
  id("com.github.johnrengelman.shadow") version "6.1.0" apply false
}

allprojects {
  group = "xyz.jpenilla"
  version = "1.3.4+${lastCommitHash()}-SNAPSHOT"
  description = "Monitor your server's performance in real time"
}

subprojects {
  apply<JavaLibraryPlugin>()
  apply<IndraPlugin>()
  apply<IndraCheckstylePlugin>()
  apply<IndraLicenseHeaderPlugin>()

  repositories {
    //mavenLocal()
    mavenCentral()
    jcenter()
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

  tasks {
    withType<JavaCompile> {
      options.compilerArgs.add("-Xlint:-processing")
    }
    withType<Jar> {
      onlyIf { archiveClassifier.get() != "javadoc" }
    }
    withType<Javadoc> {
      onlyIf { false }
    }
  }
}

tasks.withType<Jar> {
  onlyIf { false }
}

fun lastCommitHash(): String = ByteArrayOutputStream().apply {
  exec {
    commandLine = listOf("git", "rev-parse", "--short", "HEAD")
    standardOutput = this@apply
  }
}.toString(Charsets.UTF_8.name()).trim()
