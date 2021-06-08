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
    minecraftVersion("1.16.5")
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
