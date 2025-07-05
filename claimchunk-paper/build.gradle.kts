import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.filter

// TODO: See `/convention-plugins/src/main/kotlin/SharedBuildInfo.kt`

plugins {
    id("java-common-conventions")

    alias(libs.plugins.lombok)
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

dependencies {
    compileOnly(libs.annotations)
    compileOnly(libs.paper.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.worldedit.core)
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.placeholder.api)

    // We need these during runtime!
    implementation(libs.sqlite.jdbc)
    implementation("org.eclipse.persistence:javax.persistence:2.1.0")
    implementation("javax.transaction:transaction-api:1.1")
    implementation("com.github.h-thurow:q2o:3.17")
    implementation(libs.bstats)
    implementation("org.sormula:sormula:4.3")

    testImplementation(libs.slf4j.simple)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockbukkit)
    testImplementation(libs.paper.api)
    testRuntimeOnly(libs.junit.launcher)
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.mikeprimm.com")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
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
