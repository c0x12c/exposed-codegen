package dev.spartan

object Dependencies {

  object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
  }

  object Database {
    const val postgresql = "org.postgresql:postgresql:${Versions.postgresql}"
    const val postgisJdbc = "net.postgis:postgis-jdbc:${Versions.postgis}"
    const val hikari = "com.zaxxer:HikariCP:${Versions.hikari}"
  }

  object Exposed {
    const val core = "org.jetbrains.exposed:exposed-core:${Versions.exposed}"
    const val jdbc = "org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}"
    const val dao = "org.jetbrains.exposed:exposed-dao:${Versions.exposed}"
    const val javaTime = "org.jetbrains.exposed:exposed-java-time:${Versions.exposed}"
  }

  object Testing {
    const val jupiter = "org.junit.jupiter:junit-jupiter:${Versions.junit5}"
    const val mockk = "io.mockk:mockk:1.12.4"
    const val strickt = "io.strikt:strikt-jvm:0.34.0"
  }

  object Codegen {
    const val kotlinPoetMetadata = "com.squareup:kotlinpoet-metadata:1.9.0"
    const val kotlinPoetMetadataSpecs = "com.squareup:kotlinpoet-metadata-specs:1.9.0"
    const val kotlinPoetClassInspectorElement = "com.squareup:kotlinpoet-classinspector-elements:1.9.0"
    const val kotlinPoet = "com.squareup:kotlinpoet:${Versions.kotlinPoet}"
    const val kotlinPoetKsp = "com.squareup:kotlinpoet-ksp:${Versions.kotlinPoet}"
    const val ksp = "com.google.devtools.ksp:symbol-processing-api:${Versions.ksp}"
    const val autoServiceAnnotation = "com.google.auto.service:auto-service-annotations:1.0.1"
    const val autoService = "com.google.auto.service:auto-service:1.0-rc7"
    const val autoServiceKsp = "dev.zacsweers.autoservice:auto-service-ksp:1.0.0"
    const val kotlinCompileTestingKsp = "com.github.tschuchortdev:kotlin-compile-testing-ksp:${Versions.kspTesting}"
    const val kotlinCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing:${Versions.kspTesting}"
  }
}
