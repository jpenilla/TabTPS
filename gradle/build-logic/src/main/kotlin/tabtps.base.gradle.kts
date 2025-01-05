import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType

plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.licenser.spotless")
  id("net.kyori.indra.git")
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

indra {
  javaVersions {
    target(8)
    minimumToolchain(21)
  }
  github("jpenilla", "TabTPS")
  mitLicense()
}

tasks {
  withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-processing")
  }
  listOf(javadoc, javadocJar).forEach {
    it.configure {
      onlyIf { false }
    }
  }
}
