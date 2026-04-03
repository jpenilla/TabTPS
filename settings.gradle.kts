enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral {
      mavenContent { releasesOnly() }
    }
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent {
        snapshotsOnly()
        includeGroup("xyz.jpenilla")
        includeModule("org.incendo", "cloud-sponge")
      }
    }
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.spongepowered.org/repository/maven-snapshots/") {
      mavenContent { snapshotsOnly() }
    }
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.jpenilla.xyz/snapshots/")
  }
  includeBuild("gradle/build-logic")
}

plugins {
  id("xyz.jpenilla.quiet-fabric-loom-repositories") version "1.15-SNAPSHOT"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("net.neoforged.moddev.repositories") version "2.0.141"
}

rootProject.name = "TabTPS"

listOf(
  "common",
  "paper",
  "sponge",
  "fabric",
  "neoforge",
).forEach { module ->
  include("tabtps-$module")
  project(":tabtps-$module").projectDir = file(module)
}
