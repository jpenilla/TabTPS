import ca.stellardrift.build.localization.TemplateType

plugins {
  id("net.kyori.blossom")
  id("ca.stellardrift.localization")
}

localization {
  templateType.set(TemplateType.JAVA)
  templateFile.set(projectDir.resolve("src/main/template/messages.java.tmpl"))
}

tasks.jar {
  from(rootProject.file("license.txt")) {
    rename { "license_${rootProject.name.toLowerCase()}.txt" }
  }
}

dependencies {
  compileOnlyApi(libs.gson)
  compileOnlyApi(libs.guava)
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

blossom {
  replaceToken("\${VERSION}", version.toString(), "src/main/java/xyz/jpenilla/tabtps/common/util/Constants.java")
}
