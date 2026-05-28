pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
plugins {
    id("dev.victorlpgazolli.decentralized-cache-plugin") version "1.1.0"
}

buildCache {
    local { isEnabled = false }

    remote<dev.victorlpgazolli.DecentralizedConfiguration> {
        isEnabled = true
        isPush    = true
        baseIpns = "/ipns/gradle.victorlpgazolli.dev"
        verbose = true
    }
}

rootProject.name = "sample"
