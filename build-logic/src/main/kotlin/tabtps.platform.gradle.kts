plugins {
  id("tabtps.base")
  id("com.github.johnrengelman.shadow")
  id("com.modrinth.minotaur")
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

modrinth {
  projectId.set("cUhi3iB2")
  versionType.set("release")
  file.set(platformExt.productionJar)
  changelog.set(releaseNotes)
  token.set(providers.environmentVariable("MODRINTH_TOKEN"))
}
