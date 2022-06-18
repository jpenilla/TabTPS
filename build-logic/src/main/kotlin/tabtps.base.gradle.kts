import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType

plugins {
  id("net.kyori.indra")
  id("net.kyori.indra.publishing")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.license-header")
  id("net.kyori.indra.git")
}

indra {
  javaVersions {
    target(8)
    minimumToolchain(17)
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
