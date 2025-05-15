plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.freefair.lombok:io.freefair.lombok.gradle.plugin:8.13.1")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.6")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.31.0")
}
