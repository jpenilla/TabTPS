import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
  id("tabtps.platform.shadow")
  alias(libs.plugins.sponge.gradle)
  id("net.neoforged.moddev")
}

// Workaround weird interaction between VanillaGradle and shadow
val shade: Configuration by configurations.creating
configurations.implementation {
  extendsFrom(shade)
}

dependencies {
  compileOnly(libs.mixin)
  shade(libs.cloudSponge)
  shade(projects.tabtpsCommon) {
    exclude("org.slf4j")
  }
}

java {
  disableAutoTargetJvm()
}

sponge {
  injectRepositories(false)
  apiVersion("17.0.0-SNAPSHOT")
  plugin(rootProject.name.lowercase()) {
    loader {
      name(PluginLoaders.JAVA_PLAIN)
      version("1.0")
    }
    displayName(rootProject.name)
    entrypoint("xyz.jpenilla.tabtps.sponge.TabTPSPlugin")
    description(project.description)
    links {
      val url = "https://github.com/jpenilla/TabTPS"
      homepage(url)
      source(url)
      issues("$url/issues")
    }
    contributor("jmp") {
      description("Lead Developer")
    }
    license("MIT")
    dependency("spongeapi") {
      loadOrder(PluginDependency.LoadOrder.AFTER)
      optional(false)
    }
  }
}

neoForge {
  enable {
    neoFormVersion = libs.versions.neoForm.get()
  }
}

tasks {
  jar {
    archiveClassifier.set("unshaded")
    manifest {
      attributes("MixinConfigs" to "tabtps-sponge.mixins.json")
    }
  }
  shadowJar {
    configurations = listOf(shade)
    archiveClassifier.set(null as String?)
    sequenceOf(
      "net.kyori.adventure.text.feature.pagination",
      "net.kyori.adventure.serializer.configurate4",
      "org.incendo.cloud",
      "org.spongepowered.configurate",
      "xyz.jpenilla.jmplib"
    ).forEach { pkg ->
      relocate(pkg, "${rootProject.group}.${rootProject.name.lowercase()}.lib.$pkg")
    }
    dependencies {
      exclude {
        it.moduleGroup == "net.kyori"
          && it.moduleName != "adventure-serializer-configurate4"
          && it.moduleName != "adventure-text-feature-pagination"
      }
      exclude(dependency("io.leangen.geantyref:geantyref"))
    }
  }
}

tabTPSPlatform {
  productionJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}

publishMods.modrinth {
  modLoaders.add("sponge")
  minecraftVersions.addAll(
    "1.21.11"
  )
}

configurations.spongeRuntime {
  resolutionStrategy {
    eachDependency {
      if (target.name == "spongevanilla") {
        useVersion("1.21.11-18.+")
      }
    }
  }
}
