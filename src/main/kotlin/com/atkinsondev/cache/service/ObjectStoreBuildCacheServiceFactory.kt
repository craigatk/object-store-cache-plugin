package com.atkinsondev.cache.service

import com.atkinsondev.cache.ObjectStoreBuildCache
import com.atkinsondev.cache.client.BucketMissingException
import com.atkinsondev.cache.client.ObjectStoreClient
import mu.KotlinLogging
import org.gradle.caching.BuildCacheException
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

private val logger = KotlinLogging.logger {}

class ObjectStoreBuildCacheServiceFactory : BuildCacheServiceFactory<ObjectStoreBuildCache> {
    override fun createBuildCacheService(
        objectStoreBuildCache: ObjectStoreBuildCache,
        describer: BuildCacheServiceFactory.Describer,
    ): BuildCacheService {
        val accessKey = objectStoreBuildCache.accessKey
        val secretKey = objectStoreBuildCache.secretKey
        if (accessKey == null || secretKey == null) {
            logger.error(MISSING_KEYS_ERROR_MESSAGE)
            throw IllegalArgumentException(MISSING_KEYS_ERROR_MESSAGE)
        }

        val objectStoreClient =
            ObjectStoreClient(
                objectStoreBuildCache.endpoint,
                accessKey,
                secretKey,
                objectStoreBuildCache.region,
            )

        val buildCacheService = createBuildCacheService(objectStoreClient, objectStoreBuildCache)

        if (objectStoreBuildCache.autoCreateBucket) {
            objectStoreClient.createBucketIfNotExists(objectStoreBuildCache.bucket)
        } else if (!objectStoreClient.bucketExists(objectStoreBuildCache.bucket)) {
            throw BucketMissingException(objectStoreBuildCache.bucket)
        }

        objectStoreBuildCache.expirationInDays?.let {
            objectStoreClient.setBucketExpiration(objectStoreBuildCache.bucket, it)
        }

        return buildCacheService
    }

    private fun createBuildCacheService(
        objectStoreClient: ObjectStoreClient,
        objectStoreBuildCache: ObjectStoreBuildCache,
    ): ObjectStoreBuildCacheService =
        try {
            objectStoreClient.bucketExists(objectStoreBuildCache.bucket)

            ObjectStoreBuildCacheService(objectStoreBuildCache.bucket, objectStoreClient)
        } catch (e: Exception) {
            logger.error("Error connecting to build cache object store ${objectStoreBuildCache.endpoint}", e)
            throw BuildCacheException(
                "Error connecting to build cache object store ${objectStoreBuildCache.endpoint}",
                e,
            )
        }

    companion object {
        const val MISSING_KEYS_ERROR_MESSAGE = "Missing access key or secret key, disabling object store remote build cache"
    }
}
