plugins {
  id("fabric-loom") version "0.5-SNAPSHOT"
  id("com.github.johnrengelman.shadow") version "6.1.0"
  id("net.kyori.blossom") version "1.1.0"
}

configurations {
  create("shade")
}

val mcVersion = "1.16.5"

dependencies {
  minecraft("com.mojang", "minecraft", mcVersion)
  mappings(minecraft.officialMojangMappings())
  modImplementation("net.fabricmc", "fabric-loader", "0.11.1")
  modImplementation("net.fabricmc.fabric-api", "fabric-api", "0.29.4+1.16")

  add("shade", implementation(project(":tabtps-common")) {
    exclude("cloud.commandframework")
    exclude("net.kyori")
    exclude("org.slf4j")
  })

  val cloudVersion = "1.5.0-SNAPSHOT"
  modImplementation(include("cloud.commandframework", "cloud-fabric", cloudVersion))
  implementation(include("cloud.commandframework", "cloud-minecraft-extras", cloudVersion))

  modImplementation(include("net.kyori", "adventure-platform-fabric", "4.0.0-SNAPSHOT"))
  implementation(include("net.kyori", "adventure-text-feature-pagination", "4.0.0-SNAPSHOT"))
  implementation(include("net.kyori", "adventure-text-minimessage", "4.1.0-SNAPSHOT"))
  add("shade", implementation("net.kyori", "adventure-serializer-configurate4", "4.4.0") {
    exclude("*")
  })

  implementation(include("org.slf4j", "slf4j-api", "1.7.30"))
  implementation(include("org.apache.logging.log4j", "log4j-slf4j-impl", "2.8.1"))
}

tasks {
  shadowJar {
    configurations = listOf(project.configurations.getByName("shade"))
    from(rootProject.projectDir.resolve("license.txt"))
    minimize()
    listOf(
      "net.kyori.adventure.serializer.configurate4",
      "org.apache.logging",
      "org.slf4j",
      "io.leangen.geantyref",
      "org.spongepowered.configurate",
      "com.typesafe.config",
      "org.checkerframework"
    ).forEach { pkg ->
      relocate(pkg, "${rootProject.group}.${rootProject.name.toLowerCase()}.lib.$pkg")
    }
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
        "{description}" to project.description
      ).entries.forEach { (k, v) -> filter { it.replace(k, v as String) } }
    }
  }
}

blossom {
  replaceToken("{version}", version.toString(), "src/main/java/xyz/jpenilla/tabtps/fabric/TabTPSFabric.java")
}
