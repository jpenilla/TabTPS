dependencies {
  compileOnlyApi("com.google.code.gson", "gson", "2.8.0")
  compileOnlyApi("com.google.guava", "guava", "21.0")
  val cloudVersion = "1.5.0-SNAPSHOT"
  api("cloud.commandframework", "cloud-core", cloudVersion)
  api("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)
  api("org.spongepowered", "configurate-hocon", "4.1.0-SNAPSHOT")
  api("net.kyori", "adventure-serializer-configurate4", "4.4.0")
  api("net.kyori", "adventure-api", "4.4.0")
  api("net.kyori", "adventure-text-feature-pagination", "4.0.0-SNAPSHOT")
  api("net.kyori", "adventure-text-minimessage", "4.1.0-SNAPSHOT")
  api("org.slf4j", "slf4j-api", "1.7.30")
}
