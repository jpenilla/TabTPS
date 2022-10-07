plugins {
  `kotlin-dsl`
}

repositories {
  gradlePluginPortal()
}

dependencies {
  implementation(libs.build.indraCommon)
  implementation(libs.build.shadow)
}
