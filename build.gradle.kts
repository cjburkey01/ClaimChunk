plugins { alias(libs.plugins.spotless) }

spotless {
    kotlin {
        target("convention-plugins/**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktfmt().kotlinlangStyle()
    }
    kotlinGradle {
        target("*.gradle.kts", "convention-plugins/**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        ktfmt().kotlinlangStyle()
    }
}

repositories { mavenCentral() }
