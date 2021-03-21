plugins {
  id("com.github.johnrengelman.shadow")
  id("kr.entree.spigradle") version "2.2.3"
}

dependencies {
  implementation(project(":tabtps-common"))

  compileOnly("com.destroystokyo.paper", "paper-api", "1.16.5-R0.1-SNAPSHOT")
  implementation("io.papermc", "paperlib", "1.0.6")
  implementation("xyz.jpenilla", "jmplib", "1.0.1+33-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "2.2.1")
  implementation("org.slf4j", "slf4j-jdk14","1.7.30")

  implementation("cloud.commandframework", "cloud-paper", "1.5.0-SNAPSHOT")
  implementation("me.lucko", "commodore", "1.9") {
    exclude("com.mojang")
  }
}

tasks {
  shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("${project.name}-${project.version}.jar")
    destinationDirectory.set(rootProject.rootDir.resolve("build").resolve("libs"))
    from(rootProject.projectDir.resolve("license.txt"))
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

spigot {
  name = rootProject.name
  apiVersion = "1.13"
  website = "https://github.com/jpenilla/TabTPS"
  loadBefore("Essentials")
  softDepends("PlaceholderAPI", "ViaVersion")
  authors("jmp")
}
