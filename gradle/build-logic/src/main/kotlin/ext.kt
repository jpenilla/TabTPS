import net.kyori.indra.git.IndraGitExtension
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the

val Project.releaseNotes: Provider<String>
  get() = providers.environmentVariable("RELEASE_NOTES")

fun Project.lastCommitHash(): String =
  the<IndraGitExtension>().commit().orNull?.name?.substring(0, 7)
    ?: error("Could not determine git commit hash")

fun Project.decorateVersion() {
  val ver = version as String
  version = if (ver.endsWith("-SNAPSHOT")) {
    "$ver+${lastCommitHash()}"
  } else {
    ver
  }
}

val bukkitVersions = listOf(
  "1.8.8",
  "1.8.9",
  "1.9.4",
  "1.10.2",
  "1.11.2",
  "1.12.2",
  "1.13.2",
  "1.14.4",
  "1.15.2",
  "1.16.5",
  "1.17.1",
  "1.18.2",
  "1.19.4",
  "1.20.6",
  "1.21.11",
)
