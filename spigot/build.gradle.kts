plugins {
  id("com.github.johnrengelman.shadow")
  id("net.minecrell.plugin-yml.bukkit")
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
  shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("${project.name}-${project.version}.jar")
    destinationDirectory.set(rootProject.rootDir.resolve("build").resolve("libs"))
    minimize()
    listOf(
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
