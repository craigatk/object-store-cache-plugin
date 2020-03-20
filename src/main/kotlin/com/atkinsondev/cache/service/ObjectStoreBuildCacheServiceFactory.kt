package com.atkinsondev.cache.service

import com.atkinsondev.cache.ObjectStoreBuildCache
import com.atkinsondev.cache.client.BucketMissingException
import com.atkinsondev.cache.client.ObjectStoreClient
import mu.KotlinLogging
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

private val logger = KotlinLogging.logger {}

class ObjectStoreBuildCacheServiceFactory : BuildCacheServiceFactory<ObjectStoreBuildCache> {
    override fun createBuildCacheService(objectStoreBuildCache: ObjectStoreBuildCache, describer: BuildCacheServiceFactory.Describer): BuildCacheService? {
        val objectStoreClient = ObjectStoreClient(
                objectStoreBuildCache.endpoint,
                objectStoreBuildCache.accessKey,
                objectStoreBuildCache.secretKey,
                objectStoreBuildCache.region
        )

        val maybeBuildCacheService = conditionallyCreateBuildCacheService(objectStoreClient, objectStoreBuildCache)

        if (maybeBuildCacheService != null) {
            if (objectStoreBuildCache.autoCreateBucket) {
                objectStoreClient.createBucketIfNotExists(objectStoreBuildCache.bucket)
            } else if (!objectStoreClient.bucketExists(objectStoreBuildCache.bucket)) {
                throw BucketMissingException(objectStoreBuildCache.bucket)
            }
        }

        return maybeBuildCacheService
    }

    private fun conditionallyCreateBuildCacheService(
        objectStoreClient: ObjectStoreClient,
        objectStoreBuildCache: ObjectStoreBuildCache
    ): ObjectStoreBuildCacheService? =
        try {
            objectStoreClient.bucketExists(objectStoreBuildCache.bucket)

            ObjectStoreBuildCacheService(objectStoreBuildCache.bucket, objectStoreClient)
        } catch (e: Exception) {
            logger.error("Error connecting to build cache object store ${objectStoreBuildCache.endpoint}, remote cache disabled", e)
            null
        }
}
