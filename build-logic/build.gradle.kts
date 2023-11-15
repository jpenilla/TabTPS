plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  implementation(libs.build.indraCommon)
  implementation(libs.build.indraLicenser)
  implementation(libs.build.shadow)
  implementation(libs.build.hangarPublishPlugin)
  implementation(libs.build.mod.publish.plugin)
}
