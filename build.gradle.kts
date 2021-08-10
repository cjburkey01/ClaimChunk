import org.apache.tools.ant.filters.ReplaceTokens;

plugins {
    java;

    // Download files
    id("de.undercouch.download") version "4.1.2";
    // Fun annotations
    id("io.freefair.lombok") version "5.3.3.3";
    // Including dependencies in final jar
    id("com.github.johnrengelman.shadow") version "7.0.0";
}

object DepData {
    const val LIVE_VERSION = "0.0.22";
    const val PLUGIN_NAME = "ClaimChunk";
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
    const val TEST_SERVER_DIR = new File("./run/");
    const val OUTPUT_DIR = new File("./OUT/");

    // Readme locations
    const val README_IN = "./unbuilt_readme.md";
    const val README_OUT = "./README.md";

    // Tokens to replace within files
    val REPLACE_TOKENS = mapOf(
        "PLUGIN_VERSION"    to project.version,
        "MAIN_CLASS"        to MAIN_CLASS,
        "PLUGIN_NAME"       to PLUGIN_NAME,
        "LIVE_VERSION"      to LIVE_VERSION,
        "SPIGOT_VERSION"    to SPIGOT_VERSION.substring(0, SPIGOT_VERSION.indexOf("-")),
        "LATEST_MC_VERSION" to LATEST_MC_VERSION
    );
}


// Plugin information
group   = "com.cjburkey";
version = "0.0.23-prev8";

// Use Java 16 :)
extensions.configure<JavaPluginExtension>() {
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

tasks {
    withType<JavaCompile>().configureEach {
        // Disable incremental compilation (module system bs and spigot no mesh
        // well)
        options.isIncremental = false;
    }

    withType<ShadowJar>().configureEach {
        // Set the jar name and version
        archiveBaseName.set(project.archivesBaseName);
        archiveClassifier.set("plugin");
        archiveVersion.set(project.version);

        // None of these dependencies need to be in the shaded jar, and I"m not
        // 100% sure how to keep Shadow from adding everything and just adding one
        // or two simple shaded dependencies.
        dependencies {
            exclude(it.dependency("org.jetbrains:annotations:.*"));
            exclude(it.dependency("org.spigotmc:spigot-api:.*"));
            exclude(it.dependency("net.milkbowl.vault:VaultAPI:.*"));
            exclude(it.dependency("com.sk89q.worldedit:worldedit-core:.*"));
            exclude(it.dependency("com.sk89q.worldguard:worldguard-bukkit:.*"));
            exclude(it.dependency("me.clip:placeholderapi:.*"));
            exclude(it.dependency("org.junit.jupiter:junit-jupiter-api:.*"));
            exclude(it.dependency("org.junit.jupiter:junit-jupiter-engine:.*"));
        }

        // Move SmartCommandDispatcher to a unique package to avoid clashes with
        // any other plugins that might include it in their jar files.
        relocate("de.goldmensch.commanddispatcher",
            "claimchunk.dependency.de.goldmensch.commanddispatcher");
    }

    withType<Test>().configureEach {
        useJUnitPlatform();

        systemProperties = mapOf(
            "junit.jupiter.conditions.deactivate"               to "*",
            "junit.jupiter.extensions.autodetection.enabled"    to "true",
            "junit.jupiter.testinstance.lifecycle.default"      to "per_class"
        );
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


// -- BUILDING README -- //

// TODO


// Fill in readme placeholders
task updateReadme {
    // Set the inputs and outputs for the operation
    inputs.file(project.ext.readmeIn);
    outputs.file(project.ext.readmeOut);

    // Copy the new readme
    doLast {
        copy {
            from(project.ext.readmeIn);
            into(file(project.ext.readmeOut).getParent());
            rename { n -> file(project.ext.readmeOut).getName() }
            filter(ReplaceTokens, tokens: project.ext.tokens);
        }
    }
}

clean {
    // Delete old build(s)
    project.delete(files(project.ext.outputDir));

    // Delete old build(s) from test server plugin dir
    project.delete(fileTree(new File(testServerDir, "/plugins/"))
            .include("claimchunk**.jar"));
}

// Replace placeholders with values in source and resource files
processResources {
    filter(ReplaceTokens, tokens: project.ext.tokens)
}


// -- BUILDING JARS -- //


// Enable all compiler warnings for cleaner (hopefully) code
compileJava {
    options.warnings = true;
    options.deprecation = true;
    options.compilerArgs += ["-Xlint:all", "-Xmaxwarns", "200"];
    options.encoding = "UTF-8";
}

// Clear out old Spigot versions from test server directory
task deleteOldSpigotInstalls(type: Delete) {
    delete fileTree(testServerDir).include("spigot-*.jar");
}

// Download and run Spigot BuildTools to generate a Spigot server jar in the spigot `testServerDir`
task installSpigot(type: JavaExec) {
    dependsOn(deleteOldSpigotInstalls);

    mainClass = "-jar";
    workingDir(file(testServerDir));
    args(file(new File((File) testServerDir, "/BuildTools.jar")));
    doFirst {
        download {
            src(spigotBuildToolsUrl);
            dest(file(new File((File) testServerDir, "/BuildTools.jar")));
            overwrite(true);
        }
    }
}

// Copy from the libs dir to the plugins directory in the testServerDir
task copyClaimChunkToPluginsDir(type: Copy) {
    dependsOn(shadowJar);
    from(file(jar.outputs.files.singleFile));
    into(file(new File(testServerDir, "/plugins/")));
    rename("claimchunk-(.*?)-plugin.jar", "claimchunk-\$1-plugin.jar");
}

// Copy from the libs dir to the plugins directory in the testServerDir
task copyClaimChunkToOutputDir(type: Copy) {
    dependsOn(shadowJar);
    from(file(jar.outputs.files.singleFile));
    into(file(outputDir));
    rename("claimchunk-(.*?)-plugin.jar", "claimchunk-\$1-plugin.jar");
}

// When the build task is run, copy the version into the testServerDir and output
// (Also rebuild the README because Gradle and IDEA aren't getting along too well)
build.finalizedBy updateReadme;
build.finalizedBy copyClaimChunkToPluginsDir;
build.finalizedBy copyClaimChunkToOutputDir;
