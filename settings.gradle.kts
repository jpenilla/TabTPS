enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.incendo.org/content/repositories/snapshots")
    maven("https://repo.jpenilla.xyz/snapshots/")
    maven("https://repo.codemc.org/repository/maven-public")
  }
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.quiltmc.org/repository/release/")
    maven("https://repo.jpenilla.xyz/snapshots/")
  }
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0"
  id("quiet-fabric-loom") version "0.10-SNAPSHOT"
}

rootProject.name = "TabTPS"

sequenceOf(
  "common",
  "spigot",
  "fabric"
).forEach { module ->
  include("tabtps-$module")
  project(":tabtps-$module").projectDir = file(module)
}
