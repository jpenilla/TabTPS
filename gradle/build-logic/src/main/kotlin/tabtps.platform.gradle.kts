import me.modmuss50.mpp.ReleaseType

plugins {
  id("tabtps.base")
  id("me.modmuss50.mod-publish-plugin")
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

publishMods.modrinth {
  projectId.set("cUhi3iB2")
  type.set(ReleaseType.STABLE)
  file.set(platformExt.productionJar)
  changelog.set(releaseNotes)
  accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
}
