plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal() // needed to resolve plugin dependencies
}

// Plugin dependencies
dependencies {
    implementation(plugin(libs.plugins.spotless))
}

// Transforms a Gradle Plugin alias from a Version Catalog
// into a valid dependency notation for buildSrc.
fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
