import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.filter

// TODO: See `/convention-plugins/src/main/kotlin/SharedBuildInfo.kt`

plugins {
    id("java-common-conventions")

    alias(libs.plugins.shadow)
}

tasks {
    // Replace placeholders with values in source and resource files
    processResources { filter<ReplaceTokens>(getReplaceTokens()) }

    shadowJar {
        // Output to `claimchunk-x.y.z-paper.jar
        archiveBaseName = SharedBuildInfo.ARCHIVES_BASE_NAME
        archiveVersion = SharedBuildInfo.THIS_VERSION
        archiveClassifier = "paper"
    }
}

// Enable reproducible builds
// (Shadow task respects these)
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

fun getReplaceTokens(): Map<String, Map<String, String?>> {
    return mapOf(
        "tokens" to
            mapOf(
                "PLUGIN_VERSION" to SharedBuildInfo.THIS_VERSION,
                "JAVA_VERSION" to SharedBuildInfo.JAVA_VERSION.toString(),
                "MAIN_CLASS" to SharedBuildInfo.MAIN_CLASS,
                "PLUGIN_NAME" to SharedBuildInfo.PLUGIN_NAME,
                "LIVE_VERSION" to SharedBuildInfo.LIVE_VERSION,
                "SPIGOT_VERSION" to libs.paper.api.get().version,
                "LATEST_MC_VERSION" to SharedBuildInfo.LATEST_MC_VERSION,
            )
    )
}
