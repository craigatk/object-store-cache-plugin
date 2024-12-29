package com.atkinsondev.cache.service

import com.atkinsondev.cache.client.ObjectStoreClient
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ObjectStoreBuildCacheService(
    private val bucketName: String,
    private val objectStoreClient: ObjectStoreClient,
) : BuildCacheService {
    override fun store(
        cacheKey: BuildCacheKey,
        cacheEntryWriter: BuildCacheEntryWriter,
    ) {
        val outputStream = ByteArrayOutputStream()
        cacheEntryWriter.writeTo(outputStream)
        val byteArray = outputStream.toByteArray()

        objectStoreClient.putObject(bucketName, cacheKey.hashCode, byteArray.size.toLong(), ByteArrayInputStream(byteArray))
    }

    override fun load(
        cacheKey: BuildCacheKey,
        cacheEntryReader: BuildCacheEntryReader,
    ): Boolean {
        val inputStream = objectStoreClient.getObject(bucketName, cacheKey.hashCode)

        inputStream?.let { cacheEntryReader.readFrom(it) }

        return inputStream != null
    }

    override fun close() {
        // No-op - no close needed
    }
}
