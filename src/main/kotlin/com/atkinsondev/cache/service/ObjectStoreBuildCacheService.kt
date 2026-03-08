package com.atkinsondev.cache.service

import com.atkinsondev.cache.client.ObjectStoreClient
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheException
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.RuntimeException

class ObjectStoreBuildCacheService(
    private val bucketName: String,
    private val objectStoreClient: ObjectStoreClient,
    private val autoCreateBucket: Boolean,
) : BuildCacheService {
    private var bucketChecked: Boolean = false

    override fun store(
        cacheKey: BuildCacheKey,
        cacheEntryWriter: BuildCacheEntryWriter,
    ) {
        try {
            checkBucket()

            val outputStream = ByteArrayOutputStream()
            cacheEntryWriter.writeTo(outputStream)
            val byteArray = outputStream.toByteArray()

            objectStoreClient.putObject(
                bucketName,
                cacheKey.hashCode,
                byteArray.size.toLong(),
                ByteArrayInputStream(byteArray),
            )
        } catch (e: Exception) {
            throw BuildCacheException(e.message ?: "", e)
        }
    }

    override fun load(
        cacheKey: BuildCacheKey,
        cacheEntryReader: BuildCacheEntryReader,
    ): Boolean {
        try {
            checkBucket()

            val inputStream = objectStoreClient.getObject(bucketName, cacheKey.hashCode)

            inputStream?.let { cacheEntryReader.readFrom(it) }

            return inputStream != null
        } catch (e: Exception) {
            throw BuildCacheException(e.message ?: "", e)
        }
    }

    override fun close() {
        // No-op - no close needed
    }

    private fun checkBucket() {
        if (!bucketChecked) {
            if (autoCreateBucket) {
                objectStoreClient.createBucketIfNotExists(bucketName)
            } else if (!objectStoreClient.bucketExists(bucketName)) {
                throw RuntimeException(bucketName)
            }

            bucketChecked = true
        }
    }
}
