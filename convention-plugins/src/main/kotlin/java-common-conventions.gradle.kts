plugins {
    `java-library`
    id("com.diffplug.spotless")
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

tasks {
    build { dependsOn("spotlessCheck") }

    compileJava {
        // Disable incremental compilation (module system bs and spigot no mesh
        // well)
        options.isIncremental = false

        // Enable all compiler warnings for cleaner (hopefully) code
        options.isWarnings = true
        options.isDeprecation = true
        options.compilerArgs.addAll(arrayOf("-Xlint:all", "-Xmaxwarns", "200"))
        options.encoding = "UTF-8"
    }
}

spotless {
    java {
        removeUnusedImports()
        // apply a specific flavor of google-java-format
        googleJavaFormat("1.27.0").aosp().formatJavadoc(true).reorderImports(true)
        // fix formatting of type annotations
        formatAnnotations()
    }
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
    yaml {
        target("**/*.yml")
        jackson()
    }
}

repositories { mavenCentral() }
