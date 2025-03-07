import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.vanniktech.maven.publish.SonatypeHost
import org.apache.tools.ant.filters.ReplaceTokens
import de.undercouch.gradle.tasks.download.Download

plugins {
    java

    id("de.undercouch.download") version "5.6.0"
    id("io.freefair.lombok") version "8.12.1"
    // Including dependencies in final jar
    id("com.gradleup.shadow") version "8.3.6"
    id("com.vanniktech.maven.publish") version "0.31.0"
}

object DepData {
    const val JAVA_VERSION = 21

    const val LIVE_VERSION = "0.0.25-FIX3"
    const val THIS_VERSION = "0.0.26-SNAPSHOT1"
    const val PLUGIN_NAME = "ClaimChunk"
    const val ARCHIVES_BASE_NAME = "claimchunk"
    const val MAIN_CLASS = "com.cjburkey.claimchunk.ClaimChunk"

    // Only used if you run `gradlew installSpigot`
    const val SPIGOT_BUILD_TOOLS_URL
        = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar"
    const val SPIGOT_REV = "1.21"

    // Dependency versions
    const val SPIGOT_VERSION = "1.21.3-R0.1-SNAPSHOT"
    const val LATEST_MC_VERSION = "1.21.4"
    const val VAULT_API_VERSION = "1.7.1"
    const val WORLD_EDIT_CORE_VERSION = "7.3.9"
    const val WORLD_GUARD_BUKKIT_VERSION = "7.0.12"
    const val PLACEHOLDER_API_VERSION = "2.11.6"
    const val JETBRAINS_ANNOTATIONS_VERSION = "24.1.0"
    const val JUNIT_VERSION = "5.10.3"
    const val JUNIT_LAUNCHER_VERSION = "1.10.3"
    const val SQLITE_JDBC_VERSION = "3.42.0.1"
    const val JAVAX_PERSISTENCE_VERSION = "2.1.0"
    const val JAVAX_TRANSACTION_VERSION = "1.1"
    const val SANS_ORM_VERSION = "3.17"
    const val SLF4J_VERSION = "1.7.25"
    const val BSTATS_VERSION = "3.0.2"
    const val SORMULA_VERSION = "4.3"
    const val MOCK_BUKKIT_VERSION = "4.21.0"

    // Directories
    const val TEST_SERVER_DIR = "run"
    const val OUTPUT_DIR = "OUT"

    // Readme locations
    const val README_IN = "unbuilt_readme.md"
    const val README_OUT = "README.md"
}

// Tokens to replace within files
val replaceTokens = mapOf(
    "tokens" to mapOf(
        "PLUGIN_VERSION"    to DepData.THIS_VERSION,
        "JAVA_VERSION"      to DepData.JAVA_VERSION.toString(),
        "MAIN_CLASS"        to DepData.MAIN_CLASS,
        "PLUGIN_NAME"       to DepData.PLUGIN_NAME,
        "LIVE_VERSION"      to DepData.LIVE_VERSION,
        "SPIGOT_VERSION"    to DepData.SPIGOT_VERSION.substring(0, DepData.SPIGOT_VERSION.indexOf("-")),
        "LATEST_MC_VERSION" to DepData.LATEST_MC_VERSION
    )
)

// Plugin information
group   = "com.cjburkey.claimchunk"
version = DepData.THIS_VERSION

val mainDir = layout.projectDirectory

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(DepData.JAVA_VERSION)
    }
}

tasks {
    compileJava {
        mustRunAfter("googleFormat")

        // Disable incremental compilation (module system bs and spigot no mesh
        // well)
        options.isIncremental = false

        // Enable all compiler warnings for cleaner (hopefully) code
        options.isWarnings = true
        options.isDeprecation = true
        options.compilerArgs.addAll(arrayOf("-Xlint:all", "-Xmaxwarns", "200"))
        options.encoding = "UTF-8"
    }

    // We don't actually include any other libraries now
    // (except smartcommanddispatcher, but we do that manually)
    shadowJar {
        mustRunAfter("googleFormat", "updateReadme")

        // Set the jar name and version
        archiveBaseName.set(DepData.ARCHIVES_BASE_NAME)
        archiveClassifier.set("plugin")
        archiveVersion.set(project.version.toString())

        minimize {
            exclude("org/apache/log4j/**")
            exclude("*.html")
        }

        // Imma die
        dependencies {
            exclude {
                it.moduleGroup == "org.apache"
                        || it.moduleGroup == "org.slf4j"
            }
            exclude(dependency("org.slf4j:slf4j-api"))
            exclude(dependency("org.slf4j:slf4j-simple"))
            exclude(dependency("org.apache.log4j:"))
            exclude(dependency("org.xerial:sqlite-jdbc"))
            exclude(dependency("org.jetbrains:annotations"))

        }

        relocate("com.zaxxer", "claimchunk.dependency.com.zaxxer")
        relocate("javax.persistence", "claimchunk.dependency.javax.persistence")
        relocate("javax.transaction", "claimchunk.dependency.javax.transaction")
        relocate("org.eclipse", "claimchunk.dependency.org.eclipse")
        relocate("org.osgi", "claimchunk.dependency.org.osgi")
        relocate("org.bstats", "claimchunk.dependency.org.bstats")
        relocate("org.sormula", "claimchunk.dependency.org.sormula")
    }

    register<Delete>("cleanTests") {
        delete(fileTree(mainDir).include("tmp/*.tmp.sqlite3"))
    }

    test {
        useJUnitPlatform()

        systemProperties = mapOf(
            "junit.jupiter.conditions.deactivate"               to "*",
            "junit.jupiter.extensions.autodetection.enabled"    to "true",
            "junit.jupiter.testinstance.lifecycle.default"      to "per_class"
        )

        finalizedBy("cleanTests")
    }

    clean {
        // Delete old build(s)
        project.delete(files(DepData.OUTPUT_DIR))

        // Delete old build(s) from test server plugin dir
        project.delete(
            fileTree(mainDir.dir("${DepData.TEST_SERVER_DIR}/plugins"))
                .include("claimchunk**.jar")
                .include("ClaimChunk**.jar"))
    }

    build {
        mustRunAfter("googleFormat", "clean")
        dependsOn("jar")
        // When the build task is run, copy the version into the testServerDir and output
        // (Also rebuild the README file)
        finalizedBy("updateReadme",
            "copyClaimChunkToOutputDir"
        )
    }

    // Replace placeholders with values in source and resource files
    processResources {
        filter<ReplaceTokens>(replaceTokens)
    }

    // Fill in readme placeholders
    register<Copy>("updateReadme") {
        mustRunAfter("build")
        description = "Expands tokens in the unbuilt readme file into the main readme file"

        val inf = mainDir.file(DepData.README_IN)
        val ouf = mainDir.file(DepData.README_OUT)

        doFirst {
            closureOf<Delete> {
                inputs.file(ouf)
                delete(ouf)
            }
        }

        // Set the inputs and outputs for the operation
        inputs.file(inf)
        outputs.file(ouf)

        // Copy the new readme, rename it, and expand tokens
        from(inf)
        into(mainDir)
        filter<ReplaceTokens>(replaceTokens)
        rename(DepData.README_IN, DepData.README_OUT)
    }

    // Clear out old Spigot versions from test server directory
    register<Delete>("deleteOldSpigotInstalls") {
        description = "Deletes (any) old Spigot server jars from the test server directory"

        delete(fileTree(mainDir.dir(DepData.TEST_SERVER_DIR)).include("spigot-*.jar"))
    }

    register<Download>("downloadSpigotBuildTools") {
        description = "Downloads the latest version of the Spigot BuildTools into TEST_SERVER_DIR/TEMP"

        src(DepData.SPIGOT_BUILD_TOOLS_URL)
        dest(mainDir.dir(DepData.TEST_SERVER_DIR).dir("TEMP").file("BuildTools.jar"))
        overwrite(true)
    }

    // Download and run Spigot BuildTools to generate a Spigot server jar in the spigot `testServerDir`
    register<JavaExec>("installSpigot") {
        description = "Downloads and executes the Spigot build tools to generate a server jar in the test server directory."

        // Delete old Spigot jar(s) and download BuildTools first
        dependsOn("deleteOldSpigotInstalls", "downloadSpigotBuildTools")

        val testServerDir = mainDir.dir(DepData.TEST_SERVER_DIR)
        val tmpDir = testServerDir.dir("TEMP")
        val tmpServerJar = tmpDir.file("spigot-${DepData.SPIGOT_REV}.jar")

        // Run the build tools jar (the manifest main class)
        mainClass.set("-jar")
        workingDir(tmpDir)
        args("BuildTools.jar", "--nogui", "--rev", DepData.SPIGOT_REV)

        doLast {
            println("Cleaning up Spigot build")
            tmpServerJar.asFile.copyTo(testServerDir.file("spigot-${DepData.SPIGOT_REV}.jar").asFile, true)
            tmpDir.asFile.deleteRecursively()
        }
    }

    // Copy from the libs dir to the plugins directory in the testServerDir
    register<Copy>("copyClaimChunkToPluginsDir") {
        dependsOn("copyClaimChunkToOutputDir")
        description = "Copies ClaimChunk from the build directory to the test server plugin directory."

        from(shadowJar)
        into(mainDir.dir("${DepData.TEST_SERVER_DIR}/plugins"))
    }

    register<Copy>("copyClaimChunkToOutputDir") {
        mustRunAfter("clean", "build", "updateReadme")
        description = "Copies ClaimChunk from the build directory to the output directory."

        from(shadowJar)
        into(mainDir.dir(DepData.OUTPUT_DIR))
    }

    register<JavaExec>("googleFormat") {
        description = "Attempts to format source files for ClaimChunk to unify programming style."

        // For now, this file is just included with the project for the sake of
        // ease of use.
        val execJarFile = mainDir.file("req/google-java-format-1.22.0-all-deps.jar")

        // Include all source Java files
        // (I don't think there's a case where I would want to avoid formatting
        // a file, but be it necessary, this is where it would be implemented)
        val includedFiles = fileTree("src") {
            include("**/*.java")
        }.files
        inputs.files(includedFiles)
        outputs.files(includedFiles)

        // Run the build tools jar (the manifest main class)
        // The JVM args are required because of Java's Project Jigsaw
        mainClass.set("-jar")
        workingDir(mainDir)
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
        args(execJarFile)
        args("--replace")
        args("--aosp")
        args(includedFiles)
    }
}

task<Jar>("sourcesJar").mustRunAfter("googleFormat")


// -- DEPENDENCIES -- //


// Extra repos for Bukkit/Spigot stuff
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

dependencies {
    // Things needed to compile the plugin
    compileOnly("org.jetbrains:annotations:${DepData.JETBRAINS_ANNOTATIONS_VERSION}")
    compileOnly("org.spigotmc:spigot-api:${DepData.SPIGOT_VERSION}")
    compileOnly("com.github.MilkBowl:VaultAPI:${DepData.VAULT_API_VERSION}")
    compileOnly("com.sk89q.worldedit:worldedit-core:${DepData.WORLD_EDIT_CORE_VERSION}")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:${DepData.WORLD_GUARD_BUKKIT_VERSION}")
    compileOnly("me.clip:placeholderapi:${DepData.PLACEHOLDER_API_VERSION}")

    // We need these during runtime!
    implementation("org.xerial:sqlite-jdbc:${DepData.SQLITE_JDBC_VERSION}")
    implementation("org.eclipse.persistence:javax.persistence:${DepData.JAVAX_PERSISTENCE_VERSION}")
    implementation("javax.transaction:transaction-api:${DepData.JAVAX_TRANSACTION_VERSION}")
    implementation("com.github.h-thurow:q2o:${DepData.SANS_ORM_VERSION}")
    implementation("org.bstats:bstats-bukkit:${DepData.BSTATS_VERSION}")
    implementation("org.sormula:sormula:${DepData.SORMULA_VERSION}")

    testImplementation("org.slf4j:slf4j-simple:${DepData.SLF4J_VERSION}")
    testImplementation("org.junit.jupiter:junit-jupiter:${DepData.JUNIT_VERSION}")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:${DepData.MOCK_BUKKIT_VERSION}")
//    testImplementation("org.spigotmc:spigot-api:${DepData.SPIGOT_VERSION}")
    testImplementation("io.papermc.paper:paper-api:${DepData.SPIGOT_VERSION}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${DepData.JUNIT_LAUNCHER_VERSION}")
}


// -- Publishing! -- //


// https://vanniktech.github.io/gradle-maven-publish-plugin/central/
mavenPublishing {
    // publishing to https://central.sonatype.com/
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("com.cjburkey.claimchunk", DepData.ARCHIVES_BASE_NAME, DepData.THIS_VERSION)

    pom {
        name.set(DepData.PLUGIN_NAME)
        description.set("Spigot/Paper plugin allowing players to claim chunks")
        inceptionYear.set("2017")
        url.set("https://github.com/cjburkey01/ClaimChunk")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://github.com/cjburkey01/ClaimChunk/blob/main/LICENSE")
                distribution.set("https://github.com/cjburkey01/ClaimChunk/blob/main/LICENSE")
            }
        }
        developers {
            developer {
                id.set("cjburkey01")
                name.set("CJ Burkey")
                url.set("https://github.com/cjburkey01/")
            }
            developer {
                id.set("Goldmensch")
                name.set("Goldmensch")
                url.set("https://github.com/Goldmensch")
            }
            developer {
                id.set("DeathsGun")
                name.set("DeathsGun")
                url.set("https://github.com/DeathsGun")
            }
            developer {
                id.set("T0biii")
                name.set("T0biii")
                url.set("https://github.com/T0biii")
            }
            developer {
                id.set("Geolykt")
                name.set("Geolykt")
                url.set("https://github.com/Geolykt")
            }
            developer {
                id.set("JustDoom")
                name.set("JustDoom")
                url.set("https://github.com/JustDoom")
            }
            developer {
                id.set("AlexFF000")
                name.set("AlexFF000")
                url.set("https://github.com/AlexFF000")
            }
            developer {
                id.set("18PatZ")
                name.set("18PatZ")
                url.set("https://github.com/18PatZ")
            }
        }
        scm {
            url.set("https://github.com/cjburkey01/ClaimChunk")
            connection.set("scm:git:git://github.com/cjburkey01/ClaimChunk.git")
            developerConnection.set("scm:git:ssh://git@github.com/cjburkey01/ClaimChunk.git")
        }
    }
}
