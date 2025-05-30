tasks.register("build-paper-plugin") {
    group = "Project Building"
    description = "Build the fat Paper ClaimChunk plugin"

    dependsOn(":claimchunk-paper:build")
}
