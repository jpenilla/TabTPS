import ca.stellardrift.build.localization.TemplateType

plugins {
  id("tabtps.base")
  alias(libs.plugins.blossom)
  alias(libs.plugins.localization)
}

localization {
  templateType.set(TemplateType.JAVA)
  templateFile.set(projectDir.resolve("src/main/message-templates/messages.java.tmpl"))
}

tasks.jar {
  from(rootProject.file("license.txt")) {
    rename { "license_${rootProject.name.lowercase()}.txt" }
  }
}

dependencies {
  compileOnlyApi(libs.gson)
  compileOnlyApi(libs.guava)
  api(platform(libs.cloudBom))
  api(platform(libs.cloudMinecraftBom))
  api(libs.cloudCore)
  api(libs.cloudMinecraftExtras)
  api(libs.configurateHocon)
  api(platform(libs.adventureBom))
  api(libs.adventureApi)
  api(libs.adventureSerializerConfigurate4)
  api(libs.adventureTextSerializerLegacy)
  api(libs.adventureTextFeaturePagination)
  api(libs.minimessage)
  api(libs.slf4jApi)
}

sourceSets {
  main {
    blossom {
      javaSources {
        property("version", project.version.toString())
      }
    }
  }
}
