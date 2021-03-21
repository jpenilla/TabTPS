plugins {
  id("net.kyori.blossom") version "1.1.0"
}

dependencies {
  compileOnlyApi("com.google.code.gson", "gson", "2.8.0")
  compileOnlyApi("com.google.guava", "guava", "21.0")
  val cloudVersion = "1.5.0-SNAPSHOT"
  api("cloud.commandframework", "cloud-core", cloudVersion)
  api("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
  api("org.spongepowered", "configurate-hocon", "4.1.0-SNAPSHOT")
  val adventureVersion = "4.7.0"
  api("net.kyori", "adventure-serializer-configurate4", adventureVersion)
  api("net.kyori", "adventure-api", adventureVersion)
  api("net.kyori", "adventure-text-serializer-legacy", adventureVersion)
  api("net.kyori", "adventure-text-feature-pagination", "4.0.0-SNAPSHOT")
  api("net.kyori", "adventure-text-minimessage", "4.1.0-SNAPSHOT")
  api("org.slf4j", "slf4j-api", "1.7.30")
}

blossom {
  replaceToken("\${VERSION}", version.toString(), "src/main/java/xyz/jpenilla/tabtps/common/util/Constants.java")
}
