plugins {
  id("fabric-loom")
  id("com.github.johnrengelman.shadow")
}

val shade: Configuration by configurations.creating
val mcVersion = libs.versions.fabricMinecraft.get()

dependencies {
  minecraft(libs.fabricMinecraft)
  mappings(minecraft.officialMojangMappings())
  modImplementation(libs.fabricLoader)
  modImplementation(libs.fabricApi)

  implementation(projects.tabtpsCommon)
  shade(projects.tabtpsCommon) {
    isTransitive = false
  }

  modImplementation(libs.cloudFabric)
  include(libs.cloudFabric)
  implementation(libs.cloudMinecraftExtras)
  include(libs.cloudMinecraftExtras)

  modImplementation(libs.adventurePlatformFabric)
  include(libs.adventurePlatformFabric)
  implementation(libs.adventureTextFeaturePagination)
  include(libs.adventureTextFeaturePagination)
  implementation(libs.minimessage)
  include(libs.minimessage)
  implementation(libs.adventureTextSerializerLegacy)
  include(libs.adventureTextSerializerLegacy)

  implementation(libs.bundles.configurate)
  include(libs.bundles.configurate)
  implementation(libs.adventureSerializerConfigurate4)
  include(libs.adventureSerializerConfigurate4)

  implementation(libs.slf4jApi)
  include(libs.slf4jApi)
  implementation(libs.log4jSlf4jImpl)
  include(libs.log4jSlf4jImpl)
}

tasks {
  shadowJar {
    configurations = listOf(shade)
  }
  remapJar {
    dependsOn(shadowJar)
    archiveClassifier.set("")
    archiveFileName.set("${project.name}-mc$mcVersion-${project.version}.jar")
    destinationDirectory.set(rootProject.rootDir.resolve("build").resolve("libs"))
    input.set(shadowJar.get().outputs.files.singleFile)
  }
  processResources {
    filesMatching("fabric.mod.json") {
      mapOf(
        "{project.name}" to project.name,
        "{rootProject.name}" to rootProject.name,
        "{version}" to version.toString(),
        "{description}" to project.description,
        "{github}" to "https://github.com/jpenilla/TabTPS"
      ).entries.forEach { (k, v) -> filter { it.replace(k, v as String) } }
    }
  }
}
