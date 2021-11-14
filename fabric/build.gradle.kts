plugins {
  id("quiet-fabric-loom")
  id("com.github.johnrengelman.shadow")
}

val shade: Configuration by configurations.creating
val minecraftVersion = libs.versions.fabricMinecraft.get()

dependencies {
  minecraft(libs.fabricMinecraft)
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
}

indra {
  javaVersions().target(16)
}

tasks {
  runServer {
    standardInput = System.`in`
  }
  shadowJar {
    configurations = listOf(shade)
  }
  jar {
    archiveClassifier.set("dev")
  }
  remapJar {
    input.set(shadowJar.flatMap { it.archiveFile })
    archiveFileName.set("${project.name}-mc$minecraftVersion-${project.version}.jar")
    doLast {
      val archive = archiveFile.get().asFile
      archive.copyTo(rootProject.layout.buildDirectory.dir("libs").get().asFile.resolve(archive.name), overwrite = true)
    }
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
