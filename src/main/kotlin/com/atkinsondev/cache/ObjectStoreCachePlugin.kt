package com.atkinsondev.cache

import com.atkinsondev.cache.service.ObjectStoreBuildCacheServiceFactory
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class ObjectStoreCachePlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        val buildCacheConfiguration = settings.buildCache

        buildCacheConfiguration.registerBuildCacheService(
            ObjectStoreBuildCache::class.java,
            ObjectStoreBuildCacheServiceFactory::class.java
        )
    }
}
