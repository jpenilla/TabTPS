import xyz.jpenilla.runpaper.task.RunServer

plugins {
  id("tabtps.platform.shadow")
  alias(libs.plugins.runPaper)
}

dependencies {
  implementation(projects.tabtpsCommon)

  compileOnly(libs.paperApi)
  implementation(libs.adventurePlatformBukkit)
  implementation(libs.adventureTextSerializerPlain)
  implementation(libs.paperLib)
  implementation(libs.bstatsBukkit)
  implementation(libs.slf4jJdk14)

  implementation(libs.cloudPaper)
}

tasks {
  jar {
    archiveClassifier.set("unshaded")
  }

  shadowJar {
    archiveClassifier.set(null as String?)
    sequenceOf(
      "org.slf4j",
      "org.incendo.cloud",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "org.spongepowered.configurate",
      "me.lucko.commodore",
      "org.checkerframework",
      "org.bstats",
      "xyz.jpenilla.pluginbase"
    ).forEach { pkg ->
      relocate(pkg, "${rootProject.group}.${rootProject.name.lowercase()}.lib.$pkg")
    }
    manifest {
      attributes("paperweight-mappings-namespace" to "mojang")
    }
  }

  val mcVer = libs.versions.minecraft.get()

  runServer {
    minecraftVersion(mcVer)
  }

  mapOf(
    8 to setOf(
      "1.8.8",
    ),
    11 to setOf(
      "1.9.4",
      "1.10.2",
      "1.11.2",
    ),
    17 to setOf(
      "1.12.2",
      "1.13.2",
      "1.14.4",
      "1.15.2",
      "1.16.5",
      "1.17.1",
      "1.18.2",
      "1.19.4",
    ),
    21 to setOf(
      "1.20.6",
      mcVer,
    )
  ).forEach { (javaVersion, minecraftVersions) ->
    for (version in minecraftVersions) {
      createVersionedRun(version, javaVersion)
    }
  }

  processResources {
    val replacements = mapOf(
      "version" to version.toString(),
      "description" to project.description,
      "github" to "https://github.com/jpenilla/TabTPS"
    )
    inputs.properties(replacements)
    filesMatching("plugin.yml") {
      expand(replacements)
    }
  }
}

tabTPSPlatform {
  productionJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}

fun TaskContainerScope.createVersionedRun(
  version: String,
  javaVersion: Int
) = register<RunServer>("runServer${version.replace('.', '_')}") {
  group = "tabtps"
  pluginJars.from(shadowJar.flatMap { it.archiveFile })
  minecraftVersion(version)
  systemProperty("Paper.IgnoreJavaVersion", true)
  runDirectory(file("run$version"))
  javaLauncher.set(project.javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(javaVersion))
  })
}

publishMods.modrinth {
  modLoaders.add("paper")
  minecraftVersions.addAll(bukkitVersions)
}
