plugins {
  id("quiet-fabric-loom")
  id("tabtps.platform.shadow")
}

val shade: Configuration by configurations.creating
val minecraftVersion = libs.versions.minecraft.get()

dependencies {
  minecraft(libs.minecraft)
  mappings(loom.officialMojangMappings())
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

  include(libs.adventurePlatformFabric)
  modImplementation(libs.adventurePlatformFabric)
  implementation(libs.adventureTextFeaturePagination)
  include(libs.adventureTextFeaturePagination)
  implementation(libs.adventureTextSerializerLegacy)
  include(libs.adventureTextSerializerLegacy)

  implementation(libs.bundles.configurate)
  include(libs.bundles.configurate)
  implementation(libs.adventureSerializerConfigurate4)
  include(libs.adventureSerializerConfigurate4)

  modImplementation(libs.fabricPermissionsApi)
  include(libs.fabricPermissionsApi)
}

indra {
  javaVersions().target(21)
}

tasks {
  shadowJar {
    configurations = listOf(shade)
  }
  remapJar {
    archiveFileName.set("${project.name}-mc$minecraftVersion-${project.version}.jar")
  }
  processResources {
    val replacements = mapOf(
      "mod_id" to project.name,
      "mod_name" to rootProject.name,
      "version" to version.toString(),
      "description" to project.description,
      "github" to "https://github.com/jpenilla/TabTPS"
    )
    inputs.properties(replacements)
    filesMatching("fabric.mod.json") {
      expand(replacements)
    }
  }
}

tabTPSPlatform {
  productionJar.set(tasks.remapJar.flatMap { it.archiveFile })
}

publishMods.modrinth {
  modLoaders.add("fabric")
  minecraftVersions.add(minecraftVersion)
}
