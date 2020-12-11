dependencies {
    api(project(":nms-api"))
    compileOnly("org.spigotmc", "spigot", (rootProject.ext["nmsRevisions"] as Map<String, String>)[project.name])
}