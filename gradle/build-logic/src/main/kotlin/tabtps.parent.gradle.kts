plugins {
  base
  id("io.papermc.hangar-publish-plugin")
}

hangarPublish.publications.register("plugin") {
  version.set(project.version as String)
  id.set("TabTPS")
  channel.set("Release")
  changelog.set(releaseNotes)
  apiKey.set(providers.environmentVariable("HANGAR_UPLOAD_KEY"))
  platforms {
    paper {
      jar.set(project(":tabtps-paper").the<TabTPSPlatformExtension>().productionJar)
      val vers = bukkitVersions.toMutableList()
      vers -= "1.8.8"
      vers -= "1.8.9"
      vers += "1.8"
      platformVersions.addAll(vers)
    }
  }
}
