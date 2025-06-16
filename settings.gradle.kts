rootProject.name = "claimchunk"

// Shared build logic declared in `buildSrc/src/main/kotlin/java-common-conventions.gradle.kts`

includeBuild("convention-plugins")

include("claimchunk-api", "claimchunk-paper")
