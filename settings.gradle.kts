enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.quiltmc.org/repository/release/") {
      mavenContent { releasesOnly() }
    }
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://repo.spongepowered.org/repository/maven-public/")
  }
  includeBuild("build-logic")
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "5.0.1"
  id("quiet-fabric-loom") version "1.0-SNAPSHOT"
  id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

rootProject.name = "TabTPS"

sequenceOf(
  "common",
  "spigot",
  "sponge",
  "fabric"
).forEach { module ->
  include("tabtps-$module")
  project(":tabtps-$module").projectDir = file(module)
}
