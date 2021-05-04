plugins {
  id("net.kyori.blossom") version "1.2.0"
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
