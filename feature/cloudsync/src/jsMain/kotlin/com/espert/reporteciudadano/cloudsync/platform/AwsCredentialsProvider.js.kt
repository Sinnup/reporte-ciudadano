package com.espert.reporteciudadano.cloudsync.platform

// JS stub: AWS credentials on the Web platform would be injected at build time
// by a Gradle processResources task that reads aws.properties and writes a generated
// Kotlin object. For now, this stub throws to prevent accidental use without config.
//
// Production implementation: create a Gradle task in webApp/build.gradle.kts that
// reads aws.properties and generates a file like:
//   jsMain/.../generated/AwsConfig.kt
//   object AwsConfig {
//       const val accessKeyId = "..."
//       ...
//   }
// Then replace this actual with the real read.
actual fun loadAwsCredentials(): AwsCredentials {
    throw IllegalStateException(
        "AWS credentials are not configured for the JS platform. " +
        "Implement the Gradle processResources task to generate AwsConfig.kt from aws.properties."
    )
}
