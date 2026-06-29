import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.getByType
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
    target(17)
    minimumToolchain(21)
  }
  github("jpenilla", "TabTPS")
  mitLicense()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
  add("compileOnlyApi", libs.findLibrary("jspecify").get())
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
