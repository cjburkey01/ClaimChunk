rootProject.name = "claimchunk"

// Build information location in
//      `convention-plugins/src/main/kotlin/SharedBuildInfo.kt`
// Shared build logic declared in
//      `convention-plugins/src/main/kotlin/java-common-conventions.gradle.kts`

includeBuild("convention-plugins")

include("claimchunk-api", "claimchunk-paper")
