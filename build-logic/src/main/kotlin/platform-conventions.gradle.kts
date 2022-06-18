plugins {
  id("base-conventions")
  id("com.github.johnrengelman.shadow")
}

decorateVersion()

val platformExt = extensions.create("tabTPSPlatform", TabTPSPlatformExtension::class)

tasks {
  val copyJar = register("copyJar", CopyFile::class) {
    fileToCopy.set(platformExt.productionJar)
    destination.set(platformExt.productionJar.flatMap { rootProject.layout.buildDirectory.file("libs/${it.asFile.name}") })
  }
  assemble {
    dependsOn(copyJar)
  }
}
