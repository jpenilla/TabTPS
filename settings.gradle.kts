enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.stellardrift.ca/repository/snapshots/")
  }
}

plugins {
  id("ca.stellardrift.polyglot-version-catalogs") version "5.0.0-SNAPSHOT"
}

rootProject.name = "TabTPS"

setupSubproject("tabtps-common") {
  projectDir = file("common")
}
setupSubproject("tabtps-spigot") {
  projectDir = file("spigot")
}
setupSubproject("tabtps-fabric") {
  projectDir = file("fabric")
}

inline fun setupSubproject(name: String, block: ProjectDescriptor.() -> Unit) {
  include(name)
  project(":$name").apply(block)
}
