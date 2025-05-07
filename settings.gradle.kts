enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent {
        snapshotsOnly()
        includeGroup("xyz.jpenilla")
        includeGroup("org.incendo")
        includeGroup("net.kyori") // TODO adventure mod 6.4.0 & platform 4.3.5
      }
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
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
    maven("https://repo.jpenilla.xyz/snapshots/") {
      mavenContent { snapshotsOnly() }
    }
  }
  includeBuild("gradle/build-logic")
}

plugins {
  id("quiet-fabric-loom") version "1.9-SNAPSHOT"
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
  id("net.neoforged.moddev.repositories") version "2.0.88"
}

rootProject.name = "TabTPS"

listOf(
  "common",
  "spigot",
  "sponge",
  "fabric",
  "neoforge",
).forEach { module ->
  include("tabtps-$module")
  project(":tabtps-$module").projectDir = file(module)
}
