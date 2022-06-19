import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform
import org.spongepowered.plugin.metadata.model.PluginDependency
import java.util.Locale

plugins {
  id("tabtps.platform")
  id("org.spongepowered.gradle.plugin")
  id("org.spongepowered.gradle.vanilla")
}

dependencies {
  compileOnly(libs.mixin)
  implementation(libs.cloudSponge)
  implementation(projects.tabtpsCommon)
  implementation(libs.log4jSlf4jImpl) {
    isTransitive = false
  }
}

sponge {
  injectRepositories(false)
  apiVersion("8.1.0")
  plugin(rootProject.name.toLowerCase(Locale.ENGLISH)) {
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

minecraft {
  version("1.16.5")
  platform(MinecraftPlatform.JOINED)
}

tasks {
  jar {
    archiveClassifier.set("unshaded")
    manifest {
      attributes("MixinConfigs" to "tabtps-sponge.mixins.json")
    }
  }
  shadowJar {
    archiveClassifier.set(null as String?)
    sequenceOf(
      "org.slf4j",
      "net.kyori.adventure.text.feature.pagination",
      "net.kyori.adventure.serializer.configurate4",
      "org.apache.logging.slf4j",
      "cloud.commandframework",
      "org.spongepowered.configurate",
      "com.typesafe.config",
      "xyz.jpenilla.jmplib"
    ).forEach { pkg ->
      relocate(pkg, "${rootProject.group}.${rootProject.name.toLowerCase()}.lib.$pkg")
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
