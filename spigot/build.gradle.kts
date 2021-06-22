import xyz.jpenilla.runpaper.task.RunServerTask

plugins {
  id("com.github.johnrengelman.shadow")
  id("net.minecrell.plugin-yml.bukkit")
  id("xyz.jpenilla.run-paper")
}

dependencies {
  implementation(projects.tabtpsCommon)

  compileOnly(libs.paperApi)
  implementation(libs.paperLib)
  implementation(libs.jmpLib)
  implementation(libs.bstatsBukkit)
  implementation(libs.slf4jJdk14)

  implementation(libs.cloudPaper)
  implementation(libs.commodore) {
    exclude("com.mojang")
  }
}

tasks {
  runServer {
    minecraftVersion("1.17")
  }

  mapOf(
    setOf("1.8.8", "1.9.4", "1.10.2", "1.11.2", "1.12.2") to 8,
    setOf("1.13.2", "1.14.4", "1.15.2") to 11,
    setOf("1.16.5", "1.17") to 16
  ).forEach { (minecraftVersions, javaVersion) ->
    for (version in minecraftVersions) {
      createVersionedRun(version, javaVersion)
    }
  }

  jar {
    archiveClassifier.set("unshaded")
  }
  shadowJar {
    archiveClassifier.set(null as String?)
    minimize()
    sequenceOf(
      "org.slf4j",
      "cloud.commandframework",
      "io.leangen.geantyref",
      "io.papermc.lib",
      "net.kyori",
      "org.spongepowered.configurate",
      "com.typesafe.config",
      "me.lucko.commodore",
      "org.checkerframework",
      "org.bstats",
      "xyz.jpenilla.jmplib"
    ).forEach { pkg ->
      relocate(pkg, "${rootProject.group}.${rootProject.name.toLowerCase()}.lib.$pkg")
    }
    doLast {
      val archive = archiveFile.get().asFile
      archive.copyTo(rootProject.layout.buildDirectory.dir("libs").get().asFile.resolve(archive.name), overwrite = true)
    }
  }
  build {
    dependsOn(shadowJar)
  }
}

bukkit {
  main = "xyz.jpenilla.tabtps.spigot.TabTPSPlugin"
  name = rootProject.name
  apiVersion = "1.13"
  website = "https://github.com/jpenilla/TabTPS"
  loadBefore = listOf("Essentials")
  softDepend = listOf("PlaceholderAPI", "ViaVersion")
  authors = listOf("jmp")
}

fun TaskContainerScope.createVersionedRun(
  version: String,
  javaVersion: Int
) = register<RunServerTask>("runServer${version.replace('.', '_')}") {
  group = "tabtps"
  pluginJars.from(shadowJar.flatMap { it.archiveFile })
  minecraftVersion(version)
  runDirectory(file("run$version"))
  javaLauncher.set(project.javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(javaVersion))
  })
}
