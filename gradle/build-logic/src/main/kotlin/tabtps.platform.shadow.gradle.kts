import org.gradle.api.file.DuplicatesStrategy

plugins {
  id("tabtps.platform")
  id("com.gradleup.shadow")
}

tasks {
  shadowJar {
    mergeServiceFiles()
    // Needed for mergeServiceFiles to work properly in Shadow 9+
    filesMatching("META-INF/services/**") {
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
  }
}
