plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serialization)
}

val projectGroupId = "dev.victorlpgazolli.decentralized-cache-plugin"
val versionNumber = "1.1.0"

group = "dev.victorlpgazolli"
version = versionNumber


gradlePlugin {
    plugins {
        create("decentralizedCachePlugin") {
            id = projectGroupId
            group = projectGroupId
            implementationClass = "dev.victorlpgazolli.DecentralizedCachePlugin"
            version = versionNumber
            tags.set(listOf("cache", "ipfs", "gradle", "distributed"))
            displayName = "Decentralized Gradle Build Cache (IPFS)"
            description = "Gradle build cache backed by IPFS"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.ipfs)
    implementation(libs.novaCrypto)
    implementation(libs.kotlinx.serialization)
}
