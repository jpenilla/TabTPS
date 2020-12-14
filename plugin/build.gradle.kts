plugins {
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("kr.entree.spigradle") version "2.2.3"
}

val nmsRevisions = (rootProject.ext["nmsRevisions"] as Map<String, String>).keys

dependencies {
    annotationProcessor("org.projectlombok", "lombok", "1.18.16")
    compileOnly("org.projectlombok", "lombok", "1.18.16")

    implementation("xyz.jpenilla", "jmplib", "1.0.1+26-SNAPSHOT")

    val cloudVersion = "1.2.0"
    implementation("cloud.commandframework", "cloud-paper", cloudVersion)
    implementation("cloud.commandframework", "cloud-annotations", cloudVersion)
    implementation("cloud.commandframework", "cloud-minecraft-extras", cloudVersion)

    implementation("me.lucko", "commodore", "1.9") {
        exclude("com.mojang")
    }

    implementation("org.bstats", "bstats-bukkit", "1.7")

    nmsRevisions.forEach { revision ->
        implementation(project(":$revision"))
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        //minimize() //todo: minimize jar without excluding nms-api impls
        listOf(
                "cloud.commandframework",
                "io.leangen.geantyref",
                "net.kyori",
                "me.lucko.commodore",
                "org.checkerframework",
                "org.bstats",
                "xyz.jpenilla.jmplib"
        ).forEach { pkg ->
            relocate(pkg, "${rootProject.group}.${rootProject.name.toLowerCase()}.lib.$pkg")
        }
        archiveClassifier.set("")
        archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")
        destinationDirectory.set(rootProject.rootDir.resolve("build").resolve("libs"))
    }
}

spigot {
    name = rootProject.name
    apiVersion = "1.13"
    website = "https://github.com/jmanpenilla/TabTPS"
    loadBefore("Essentials")
    softDepends("Prisma", "PlaceholderAPI", "ViaVersion")
    authors("jmp")
}