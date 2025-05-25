plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
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
