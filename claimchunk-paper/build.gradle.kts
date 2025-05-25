import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.filter

plugins {
    id("java-common-conventions")

    alias(libs.plugins.shadow)
}

object DepVersions {
    const val PAPER_VERSION = "1.21.5-R0.1-SNAPSHOT"
    const val LATEST_MC_VERSION = "1.21.5"
}

object BuildInfo {
    const val JAVA_VERSION = 21

    const val LIVE_VERSION = "0.0.25-FIX3"
    const val THIS_VERSION = "1.0.0-SNAPSHOT1"
    const val PLUGIN_NAME = "ClaimChunk"
    const val ARCHIVES_BASE_NAME = "claimchunk"
    const val MAIN_CLASS = "com.cjburkey.claimchunk.ClaimChunk"
//
//    // Directories
//    const val TEST_SERVER_DIR = "run"
//    const val OUTPUT_DIR = "OUT"
//
//    // Readme locations
//    const val README_IN = "unbuilt_readme.md"
//    const val README_OUT = "README.md"
}

// Tokens to replace within files
val replaceTokens = mapOf(
    "tokens" to mapOf(
        "PLUGIN_VERSION"    to BuildInfo.THIS_VERSION,
        "JAVA_VERSION"      to BuildInfo.JAVA_VERSION.toString(),
        "MAIN_CLASS"        to BuildInfo.MAIN_CLASS,
        "PLUGIN_NAME"       to BuildInfo.PLUGIN_NAME,
        "LIVE_VERSION"      to BuildInfo.LIVE_VERSION,
        "SPIGOT_VERSION"    to libs.paper.api.get().version,
        "LATEST_MC_VERSION" to DepVersions.LATEST_MC_VERSION
    )
)

tasks {
    // Replace placeholders with values in source and resource files
    processResources {
        filter<ReplaceTokens>(replaceTokens)
    }
}
