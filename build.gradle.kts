plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.git")
}

group = "xyz.jpenilla"
version = "1.3.10-SNAPSHOT"
  .run { if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this }
description = "Monitor your server's performance in real time"

subprojects {
  plugins.apply("java-library")
  plugins.apply("net.kyori.indra")
  plugins.apply("net.kyori.indra.publishing")
  plugins.apply("net.kyori.indra.checkstyle")
  plugins.apply("net.kyori.indra.license-header")

  indra {
    javaVersions {
      target(8)
    }
    github("jpenilla", "TabTPS")
    mitLicense()
  }

  tasks {
    withType<JavaCompile> {
      options.compilerArgs.add("-Xlint:-processing")
    }
    sequenceOf(javadoc, javadocJar).forEach {
      it.configure {
        onlyIf { false }
      }
    }
  }
}

tasks.withType<Jar> {
  onlyIf { false }
}

fun lastCommitHash(): String =
  rootProject.indraGit.commit()?.name?.substring(0, 7)
    ?: error("Could not determine git commit hash")
