import net.kyori.indra.git.IndraGitExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

fun Project.lastCommitHash(): String =
  the<IndraGitExtension>().commit()?.name?.substring(0, 7)
    ?: error("Could not determine git commit hash")

fun Project.decorateVersion() {
  val ver = version as String
  version = if (ver.endsWith("-SNAPSHOT")) {
    "$ver+${lastCommitHash()}"
  } else {
    ver
  }
}
