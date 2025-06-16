plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
    gradlePluginPortal() // needed to resolve plugin dependencies
}

// Plugin dependencies
dependencies { implementation(plugin(libs.plugins.spotless)) }

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktfmt().kotlinlangStyle()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktfmt().kotlinlangStyle()
    }
}

// Transforms a Gradle Plugin alias from a Version Catalog
// into a valid dependency notation for buildSrc.
fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
