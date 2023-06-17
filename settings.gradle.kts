pluginManagement {
  repositories {
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://maven.google.com/")
    gradlePluginPortal()
  }

  plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.8.20")
    id("com.google.devtools.ksp") version("1.8.20-1.0.11")
    id("io.github.gradle-nexus.publish-plugin") version ("1.3.0")
  }
}

rootProject.name = "exposed-codegen"

include(
  "module-codegen",
  "module-api",
  "module-test"
)
