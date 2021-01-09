plugins {
  id("com.github.johnrengelman.shadow") version "6.1.0"
  id("kr.entree.spigradle") version "2.2.3"
}

val nmsRevisions = (rootProject.ext["nmsRevisions"] as Map<String, String>).keys

dependencies {
  implementation("org.spongepowered", "configurate-hocon", "4.0.0")
  implementation("net.kyori", "adventure-serializer-configurate4", "4.3.0")
  implementation("xyz.jpenilla", "jmplib", "1.0.1+29-SNAPSHOT")
  implementation("org.bstats", "bstats-bukkit", "1.8")

  val cloudVersion = "1.4.0-SNAPSHOT"
  implementation("cloud.commandframework", "cloud-paper", cloudVersion)
  implementation("cloud.commandframework", "cloud-annotations", cloudVersion)
  implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
  implementation("me.lucko", "commodore", "1.9") {
    exclude("com.mojang")
  }

  nmsRevisions.forEach { revision ->
    implementation(project(":$revision"))
  }
}

tasks {
  shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
    destinationDirectory.set(rootProject.rootDir.resolve("build").resolve("libs"))
    from(rootProject.projectDir.resolve("license.txt"))
    minimize {
      exclude { nmsRevisions.contains(it.moduleName) }
    }
    listOf(
      "cloud.commandframework",
      "io.leangen.geantyref",
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
  website = "https://github.com/jmanpenilla/TabTPS"
  loadBefore("Essentials")
  softDepends("Prisma", "PlaceholderAPI", "ViaVersion")
  authors("jmp")
}
