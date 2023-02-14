// I NEED THESE PLEASE
@file:Suppress("RedundantSemicolon")

import org.apache.tools.ant.filters.ReplaceTokens;
import de.undercouch.gradle.tasks.download.Download;

plugins {
    java;

    id("de.undercouch.download") version "5.3.0";
    id("io.freefair.lombok") version "6.6.2";
    // Including dependencies in final jar
    id("com.github.johnrengelman.shadow") version "7.1.2";
}

object DepData {
    const val LIVE_VERSION = "0.0.23-RC8";
    const val THIS_VERSION = "0.0.24-RC1";
    const val PLUGIN_NAME = "ClaimChunk";
    const val ARCHIVES_BASE_NAME = "claimchunk";
    const val MAIN_CLASS = "com.cjburkey.claimchunk.ClaimChunk";

    // Only used if you run `gradlew installSpigot`
    const val SPIGOT_BUILD_TOOLS_URL
        = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";

    // Dependency versions
    const val BUKKIT_VERSION = "1.19.2-R0.1-SNAPSHOT";
    const val SPIGOT_VERSION = "1.19.2-R0.1-SNAPSHOT";
    const val LATEST_MC_VERSION = "1.19.2";
    const val VAULT_API_VERSION = "1.7";
    const val WORLD_EDIT_CORE_VERSION = "7.2.9";
    const val WORLD_GUARD_BUKKIT_VERSION = "7.0.7";
    const val PLACEHOLDER_API_VERSION = "2.11.1";
    const val JETBRAINS_ANNOTATIONS_VERSION = "23.0.0";
    const val JUNIT_VERSION = "5.9.0";
    // Goldmensch's SmartCommandDispatcher. Thank you!!
    const val SMART_COMMAND_DISPATCHER_VERSION = "2.0.1";
    // And internationalization library!
    const val JALL_I18N_VERSION = "1.0.2"

    // Directories
    const val TEST_SERVER_DIR = "run";
    const val OUTPUT_DIR = "OUT";

    // Readme locations
    const val README_IN = "unbuilt_readme.md";
    const val README_OUT = "README.md";
}

// Tokens to replace within files
val replaceTokens = mapOf(
    "tokens" to mapOf(
        "PLUGIN_VERSION"    to DepData.THIS_VERSION,
        "MAIN_CLASS"        to DepData.MAIN_CLASS,
        "PLUGIN_NAME"       to DepData.PLUGIN_NAME,
        "LIVE_VERSION"      to DepData.LIVE_VERSION,
        "SPIGOT_VERSION"    to DepData.SPIGOT_VERSION.substring(0, DepData.SPIGOT_VERSION.indexOf("-")),
        "LATEST_MC_VERSION" to DepData.LATEST_MC_VERSION
    )
);

// Plugin information
group   = "com.cjburkey";
version = DepData.THIS_VERSION;

val mainDir = layout.projectDirectory;

// Use Java 16 (17 was too restrictive)
extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16));
}

tasks {
    compileJava {
        // Disable incremental compilation (module system bs and spigot no mesh
        // well)
        options.isIncremental = false;

        // Enable all compiler warnings for cleaner (hopefully) code
        options.isWarnings = true;
        options.isDeprecation = true;
        options.compilerArgs.addAll(arrayOf("-Xlint:all", "-Xmaxwarns", "200"));
        options.encoding = "UTF-8";
    }

    shadowJar {
        // Set the jar name and version
        archiveBaseName.set(DepData.ARCHIVES_BASE_NAME);
        archiveClassifier.set("plugin");
        archiveVersion.set(project.version.toString());

        dependencies {
            // Exclude annotations from output jar
            exclude(dependency("org.jetbrains:annotations:.*"));
        }

        // Move SmartCommandDispatcher to a unique package to avoid clashes with
        // any other plugins that might include it in their jar files.
        relocate("de.goldmensch.commanddispatcher",
            "claimchunk.dependency.de.goldmensch.commanddispatcher");
        // Do the same with JALL
        relocate("io.github.goldmensch.jall",
            "claimchunk.dependency.io.github.goldmensch.jall");
    }

    test {
        useJUnitPlatform();

        systemProperties = mapOf(
            "junit.jupiter.conditions.deactivate"               to "*",
            "junit.jupiter.extensions.autodetection.enabled"    to "true",
            "junit.jupiter.testinstance.lifecycle.default"      to "per_class"
        );
    }

    clean {
        // Delete old build(s)
        project.delete(files(DepData.OUTPUT_DIR));

        // Delete old build(s) from test server plugin dir
        project.delete(
            fileTree(mainDir.dir("${DepData.TEST_SERVER_DIR}/plugins"))
                .include("claimchunk**.jar")
                .include("ClaimChunk**.jar"));
    }

    build {
        mustRunAfter("googleFormat")
        // When the build task is run, copy the version into the testServerDir and output
        // (Also rebuild the README file)
        finalizedBy("updateReadme",
            "copyClaimChunkToOutputDir",
            "copyClaimChunkToPluginsDir"
        );
    }

    // Replace placeholders with values in source and resource files
    processResources {
        filter<ReplaceTokens>(replaceTokens);
    }

    // Fill in readme placeholders
    register<Copy>("updateReadme") {
        mustRunAfter("shadowJar");
        description = "Expands tokens in the unbuilt readme file into the main readme file";

        val inf = mainDir.file(DepData.README_IN);
        val ouf = mainDir.file(DepData.README_OUT);

        doFirst {
            closureOf<Delete> {
                inputs.file(ouf)
                delete(ouf);
            }
        }

        // Set the inputs and outputs for the operation
        inputs.file(inf);
        outputs.file(ouf);

        // Copy the new readme, rename it, and expand tokens
        from(inf);
        into(mainDir);
        filter<ReplaceTokens>(replaceTokens);
        rename(DepData.README_IN, DepData.README_OUT)
    }

    // Clear out old Spigot versions from test server directory
    register<Delete>("deleteOldSpigotInstalls") {
        description = "Deletes (any) old Spigot server jars from the test server directory";

        delete(fileTree(mainDir.dir(DepData.TEST_SERVER_DIR)).include("spigot-*.jar"));
    }

    register<Download>("downloadSpigotBuildTools") {
        description = "Downloads the latest version of the Spigot BuildTools into TEST_SERVER_DIR/TEMP";

        src(DepData.SPIGOT_BUILD_TOOLS_URL);
        dest(mainDir.dir(DepData.TEST_SERVER_DIR).dir("TEMP").file("BuildTools.jar"));
        overwrite(true);
    }

    // Download and run Spigot BuildTools to generate a Spigot server jar in the spigot `testServerDir`
    register<JavaExec>("installSpigot") {
        description = "Downloads and executes the Spigot build tools to generate a server jar in the test server directory.";

        // Delete old Spigot jar(s) and download BuildTools first
        dependsOn("deleteOldSpigotInstalls", "downloadSpigotBuildTools");

        val testServerDir = mainDir.dir(DepData.TEST_SERVER_DIR);
        val tmpDir = testServerDir.dir("TEMP");
        val tmpServerJar = tmpDir.file("spigot-${DepData.LATEST_MC_VERSION}.jar");

        // Run the build tools jar (the manifest main class)
        mainClass.set("-jar");
        workingDir(tmpDir);
        args("BuildTools.jar");

        doLast {
            println("Cleaning up Spigot build");
            tmpServerJar.asFile.copyTo(testServerDir.file("spigot-${DepData.LATEST_MC_VERSION}.jar").asFile, true);
            tmpDir.asFile.deleteRecursively();
        }
    }

    // Copy from the libs dir to the plugins directory in the testServerDir
    register<Copy>("copyClaimChunkToPluginsDir") {
        dependsOn("shadowJar");
        mustRunAfter("copyClaimChunkToOutputDir");
        description = "Copies ClaimChunk from the build directory to the test server plugin directory.";

        val inputFile = mainDir.file("build/libs/claimchunk-${project.version}-plugin.jar");
        val outputDir = mainDir.dir("${DepData.TEST_SERVER_DIR}/plugins");

        inputs.file(inputFile);
        outputs.file(outputDir.file("claimchunk-${project.version}-plugin.jar"));

        from(mainDir.file("build/libs/claimchunk-${project.version}-plugin.jar"));
        into(outputDir);
    }

    register<Copy>("copyClaimChunkToOutputDir") {
        dependsOn("shadowJar");
        mustRunAfter("updateReadme");
        description = "Copies ClaimChunk from the build directory to the output directory.";

        val inputFile = mainDir.file("build/libs/claimchunk-${project.version}-plugin.jar");
        val outputDir = mainDir.dir(DepData.OUTPUT_DIR);

        inputs.file(inputFile);
        outputs.file(outputDir.file("claimchunk-${project.version}-plugin.jar"));

        from(inputFile);
        into(outputDir);
    }

    register<JavaExec>("googleFormat") {
        description = "Attempts to format source files for ClaimChunk to unify programming style.";

        // For now, this file is just included with the project for the sake of
        // ease of use. Perhaps I should release an updated version of the
        // plugin someone else developed, it's outdated and wouldn't work.
        // (Hence my reinventing the broken wheel here)
        val execJarFile = mainDir.file("req/google-java-format-1.11.0-all-deps.jar");

        // Include all source Java files
        // (I don't think there's a case where I would want to avoid formatting
        // a file, but be it necessary, this is where it would be implemented.
        val includedFiles = fileTree("src") {
            include("**/*.java")
        }.files;
        inputs.files(includedFiles);
        outputs.files(includedFiles);

        // Run the build tools jar (the manifest main class)
        // The JVM args are required because of Java's Project Jigsaw
        mainClass.set("-jar");
        workingDir(mainDir);
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED");
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED");
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED");
        jvmArgs("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED");
        args(execJarFile);
        args("--replace");
        args("--aosp");
        args(includedFiles);
    }
}


// -- DEPENDENCIES -- //


// Extra repos for Bukkit/Spigot stuff
repositories {
    mavenCentral();
    maven("https://oss.sonatype.org/content/repositories/snapshots/");
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/");
    maven("https://maven.enginehub.org/repo/");
    maven("https://repo.mikeprimm.com");
    maven("https://papermc.io/repo/repository/maven-public/");
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/");
    maven("https://eldonexus.de/repository/maven-public");

    // Why do you have to be special, huh?
    maven {
        url = uri("http://nexus.hc.to/content/repositories/pub_releases/");
        isAllowInsecureProtocol = true;
    }
}

dependencies {
    // Things needed to compile the plugin
    compileOnly("org.jetbrains:annotations:${DepData.JETBRAINS_ANNOTATIONS_VERSION}");
    compileOnly("org.spigotmc:spigot-api:${DepData.SPIGOT_VERSION}");
    compileOnly("net.milkbowl.vault:VaultAPI:${DepData.VAULT_API_VERSION}");
    compileOnly("com.sk89q.worldedit:worldedit-core:${DepData.WORLD_EDIT_CORE_VERSION}");
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:${DepData.WORLD_GUARD_BUKKIT_VERSION}");
    compileOnly("me.clip:placeholderapi:${DepData.PLACEHOLDER_API_VERSION}");

    // Dependencies that needs to be shaded into the final jar
    implementation("de.goldmensch:SmartCommandDispatcher:${DepData.SMART_COMMAND_DISPATCHER_VERSION}");
    implementation("io.github.goldmensch:JALL:${DepData.JALL_I18N_VERSION}");


    testCompileOnly("org.junit.jupiter:junit-jupiter-api:${DepData.JUNIT_VERSION}");
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${DepData.JUNIT_VERSION}");
}
