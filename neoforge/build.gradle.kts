import net.neoforged.moddevgradle.internal.RunGameTask

plugins {
  id("net.neoforged.moddev")
  id("tabtps.platform")
}

val minecraftVersion = libs.versions.minecraft.get()

neoForge {
  enable {
    version = libs.versions.neoforge.get()
  }

  mods {
    register("tabtps") {
      sourceSet(sourceSets.main.get())
    }
  }

  runs {
    register("client") {
      client()
      loadedMods.set(emptySet()) // Work around classpath issues by using the production jar for dev runs
    }
    register("server") {
      server()
      loadedMods.set(emptySet()) // Work around classpath issues by using the production jar for dev runs
    }
  }
}

// Work around classpath issues by using the production jar for dev runs
tasks.withType<RunGameTask>().configureEach {
  dependsOn(tasks.jar)
  doFirst {
    val jar = file("run/mods/main.jar")
    jar.parentFile.mkdirs()
    tasks.jar.get().archiveFile.get().asFile.copyTo(jar, true)
  }
}

indra {
  javaVersions().target(21)
}

val common: Configuration by configurations.creating

dependencies {
  implementation(projects.tabtpsCommon)
  common(projects.tabtpsCommon) {
    isTransitive = false
  }

  implementation(libs.cloudNeoforge)
  jarJar(libs.cloudNeoforge)
  implementation(libs.cloudMinecraftExtras)
  jarJar(libs.cloudMinecraftExtras)

  implementation(libs.adventurePlatformNeoforge)
  jarJar(libs.adventurePlatformNeoforge)
  implementation(libs.adventureTextFeaturePagination)
  jarJar(libs.adventureTextFeaturePagination)
  implementation(libs.adventureTextSerializerLegacy)
  jarJar(libs.adventureTextSerializerLegacy)

  implementation(libs.bundles.configurate)
  jarJar(libs.bundles.configurate)
  implementation(libs.adventureSerializerConfigurate4)
  jarJar(libs.adventureSerializerConfigurate4)
}

indra {
  javaVersions().target(21)
}

tasks {
  jar {
    archiveFileName.set("${project.name}-mc$minecraftVersion-${project.version}.jar")
    from(common.elements.map { zipTree(it.single()) }) {
      exclude("META-INF/MANIFEST.MF")
    }
  }
  processResources {
    val replacements = mapOf(
      "version" to version.toString(),
      "description" to project.description,
      "github" to "https://github.com/jpenilla/TabTPS"
    )
    inputs.properties(replacements)
    filesMatching("META-INF/neoforge.mods.toml") {
      expand(replacements)
    }
  }
}

tabTPSPlatform {
  productionJar.set(tasks.jar.flatMap { it.archiveFile })
}

publishMods.modrinth {
  modLoaders.add("neoforge")
  minecraftVersions.add(minecraftVersion)
}
