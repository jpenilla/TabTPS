rootProject.name = "TabTPS"

pluginManagement {
  repositories {
    maven("https://maven.fabricmc.net/")
    gradlePluginPortal()
  }
}

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
