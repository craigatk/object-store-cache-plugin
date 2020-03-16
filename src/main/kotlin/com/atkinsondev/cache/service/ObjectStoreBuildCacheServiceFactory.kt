package com.atkinsondev.cache.service

import com.atkinsondev.cache.ObjectStoreBuildCache
import com.atkinsondev.cache.client.BucketMissingException
import com.atkinsondev.cache.client.ObjectStoreClient
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

class ObjectStoreBuildCacheServiceFactory : BuildCacheServiceFactory<ObjectStoreBuildCache> {
    override fun createBuildCacheService(objectStoreBuildCache: ObjectStoreBuildCache, describer: BuildCacheServiceFactory.Describer): BuildCacheService {
        val objectStoreClient = ObjectStoreClient(
                objectStoreBuildCache.endpoint,
                objectStoreBuildCache.accessKey,
                objectStoreBuildCache.secretKey,
                objectStoreBuildCache.region
        )

        if (objectStoreBuildCache.autoCreateBucket) {
            objectStoreClient.createBucketIfNotExists(objectStoreBuildCache.bucket)
        } else if (!objectStoreClient.bucketExists(objectStoreBuildCache.bucket)) {
            throw BucketMissingException(objectStoreBuildCache.bucket)
        }

        return ObjectStoreBuildCacheService(objectStoreBuildCache.bucket, objectStoreClient)
    }
}
