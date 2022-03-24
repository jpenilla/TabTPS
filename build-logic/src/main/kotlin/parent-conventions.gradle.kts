plugins {
  base
  id("net.kyori.indra.git")
}

version = (version as String).decorateVersion()

fun lastCommitHash(): String =
  indraGit.commit()?.name?.substring(0, 7)
    ?: error("Could not determine git commit hash")

fun String.decorateVersion(): String =
  if (endsWith("-SNAPSHOT")) "$this+${lastCommitHash()}" else this
