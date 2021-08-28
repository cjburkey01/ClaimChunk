// I NEED THESE PLEASE
@file:Suppress("RedundantSemicolon")

import org.apache.tools.ant.filters.ReplaceTokens;
import de.undercouch.gradle.tasks.download.Download;

plugins {
    java;

    id("de.undercouch.download") version "4.1.2";
    id("io.freefair.lombok") version "6.1.0-m3";
    // Including dependencies in final jar
    id("com.github.johnrengelman.shadow") version "7.0.0";
}

object DepData {
    const val LIVE_VERSION = "0.0.22";
    const val THIS_VERSION = "0.0.23-prev8";
    const val PLUGIN_NAME = "ClaimChunk";
    const val ARCHIVES_BASE_NAME = "claimchunk";
    const val MAIN_CLASS = "com.cjburkey.claimchunk.ClaimChunk";

    // Only used if you run `gradlew installSpigot`
    const val SPIGOT_BUILD_TOOLS_URL
        = "https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar";

    // Dependency versions
    const val BUKKIT_VERSION = "1.17.1-R0.1-SNAPSHOT";
    const val SPIGOT_VERSION = "1.17.1-R0.1-SNAPSHOT";
    const val VAULT_API_VERSION = "1.7";
    const val WORLD_EDIT_CORE_VERSION = "7.2.6-SNAPSHOT";
    const val WORLD_GUARD_BUKKIT_VERSION = "7.0.5-SNAPSHOT";
    const val PLACEHOLDER_API_VERSION = "2.10.10-DEV-129";
    const val JETBRAINS_ANNOTATIONS_VERSION = "16.0.2";
    const val JUNIT_VERSION = "5.7.0";
    const val LATEST_MC_VERSION = "1.17.1";
    // Goldmensch's SmartCommandDispatcher
    const val SMART_COMMAND_DISPATCHER_VERSION = "1.0.5-DEV";

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

// Use Java 16 :)
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

        // None of these dependencies need to be in the shaded jar, and I"m not
        // 100% sure how to keep Shadow from adding everything and just adding one
        // or two simple shaded dependencies.
        dependencies {
            exclude(dependency("org.jetbrains:annotations:.*"));
            exclude(dependency("org.spigotmc:spigot-api:.*"));
            exclude(dependency("net.milkbowl.vault:VaultAPI:.*"));
            exclude(dependency("com.sk89q.worldedit:worldedit-core:.*"));
            exclude(dependency("com.sk89q.worldguard:worldguard-bukkit:.*"));
            exclude(dependency("me.clip:placeholderapi:.*"));
            exclude(dependency("org.junit.jupiter:junit-jupiter-api:.*"));
            exclude(dependency("org.junit.jupiter:junit-jupiter-engine:.*"));
        }

        // Move SmartCommandDispatcher to a unique package to avoid clashes with
        // any other plugins that might include it in their jar files.
        relocate("de.goldmensch.commanddispatcher",
            "claimchunk.dependency.de.goldmensch.commanddispatcher");
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
            fileTree(layout.buildDirectory.dir("${DepData.TEST_SERVER_DIR}/plugins"))
                .include("claimchunk**.jar"));
    }

    build {
        // When the build task is run, copy the version into the testServerDir and output
        // (Also rebuild the README because Gradle and IDEA aren't getting along too well)
        finalizedBy("copyClaimChunkToPluginsDir",
            "copyClaimChunkToOutputDir",
            "updateReadme");
    }

    // Replace placeholders with values in source and resource files
    processResources {
        filter<ReplaceTokens>(replaceTokens);
    }

    // Fill in readme placeholders
    register<Copy>("updateReadme") {
        dependsOn("shadowJar");

        val outDir = layout.buildDirectory;
        val inf = outDir.file(DepData.README_IN);
        val ouf = outDir.file(DepData.README_OUT);

        // Set the inputs and outputs for the operation
        inputs.file(inf);
        outputs.file(ouf);

        // Copy the new readme, rename it, and expand tokens
        from(inf);
        into(outDir);
        filter<ReplaceTokens>(replaceTokens);
    }

    // Clear out old Spigot versions from test server directory
    register<Delete>("deleteOldSpigotInstalls") {
        delete(fileTree(layout.buildDirectory.dir(DepData.TEST_SERVER_DIR)).include("spigot-*.jar"));
    }

    // Download and run Spigot BuildTools to generate a Spigot server jar in the spigot `testServerDir`
    register<JavaExec>("installSpigot") {
        // Delete old Spigot jar(s) first
        dependsOn("deleteOldSpigotInstalls");

        val buildToolsFile = layout.buildDirectory.file("${DepData.TEST_SERVER_DIR}/BuildTools.jar");

        // Download BuildTools from Spigot
        doFirst {
            closureOf<Download> {
                src(DepData.SPIGOT_BUILD_TOOLS_URL);
                dest(buildToolsFile);
                overwrite(true);
            }
        }

        // Run the build tools jar (the manifest main class)
        mainClass.set("-jar");
        workingDir(layout.buildDirectory.dir(DepData.TEST_SERVER_DIR));
        args(buildToolsFile);
    }

    // Copy from the libs dir to the plugins directory in the testServerDir
    register<Copy>("copyClaimChunkToPluginsDir") {
        dependsOn("shadowJar");
        from(layout.buildDirectory.file("build/libs/claimchunk-${project.version}-plugin.jar"));
        into(layout.buildDirectory.dir("${DepData.TEST_SERVER_DIR}/plugins"));
        rename("claimchunk-(.*?)-plugin.jar", "claimchunk-\$1-plugin.jar");
    }

    register<Copy>("copyClaimChunkToOutputDir") {
        dependsOn("shadowJar");
        from(layout.buildDirectory.file("build/libs/claimchunk-${project.version}-plugin.jar"));
        into(layout.buildDirectory.dir(DepData.OUTPUT_DIR));
        rename("claimchunk-(.*?)-plugin.jar", "claimchunk-\$1-plugin.jar");
    }
}


// -- DEPENDENCIES -- //


// Extra repos for Bukkit/Spigot stuff
repositories {
    mavenCentral();
    maven("https://oss.sonatype.org/content/repositories/snapshots/");
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/");
    maven("https://maven.sk89q.com/repo/");
    maven("https://repo.mikeprimm.com");
    maven("https://papermc.io/repo/repository/maven-public/");
    maven("https://repo.extendedclip.com/content/repositories/dev/");
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

    // Dependency that needs to be shaded into the final jar
    implementation("de.goldmensch:SmartCommandDispatcher:${DepData.SMART_COMMAND_DISPATCHER_VERSION}");

    testImplementation("org.junit.jupiter:junit-jupiter-api:${DepData.JUNIT_VERSION}");
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${DepData.JUNIT_VERSION}");
}
