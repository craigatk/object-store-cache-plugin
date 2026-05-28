package dev.victorlpgazolli.service

import dev.victorlpgazolli.client.IpfsClient
import dev.victorlpgazolli.utils.Logger
import org.gradle.caching.BuildCacheEntryReader
import org.gradle.caching.BuildCacheEntryWriter
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.BuildCacheService
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

private const val LOG_TAG = "[decentralized-cache]"
internal class IpfsBuildCacheService(
    private val ipfsClient: IpfsClient,
    private val logger: Logger,
) : BuildCacheService {
    override fun store(
        cacheKey: BuildCacheKey,
        cacheEntryWriter: BuildCacheEntryWriter,
    ) {
        val path = "/tmp/ipfs-cache-${cacheKey.hashCode}.gz"
        logger.log(LOG_TAG, cacheKey.hashCode, "Storing cache entry at $path")

        val compressedFile = File(path).apply {
            createNewFile()
            deleteOnExit()
        }

        logger.log(LOG_TAG, cacheKey.hashCode, "Writing to $path")
        GZIPOutputStream(FileOutputStream(compressedFile)).use { gzipOutputStream ->
            cacheEntryWriter.writeTo(gzipOutputStream)
            gzipOutputStream.flush()
        }


        runCatching {
            ipfsClient.putObject(
                filePath = compressedFile.absolutePath,
                objectName = cacheKey.hashCode.toString(),
            )
        }.onSuccess {
            logger.log(
                LOG_TAG,
                cacheKey.hashCode,
                "Successfully stored cache at ${compressedFile.absolutePath}"
            )
        }.onFailure { exception ->
            logger.log(
                LOG_TAG,
                cacheKey.hashCode,
                "Failed to store cache, error: ${exception.message}"
            )
        }
    }
    override fun load(
        cacheKey: BuildCacheKey,
        cacheEntryReader: BuildCacheEntryReader,
    ): Boolean {
        logger.log(LOG_TAG, cacheKey.hashCode, "Loading cache entry")
        val inputStream = ipfsClient.getObject(cacheKey.hashCode)

        if (inputStream == null) {
            logger.log(LOG_TAG, cacheKey.hashCode, "Cache entry not found")
            return false
        }

        GZIPInputStream(inputStream).use { gis ->
            cacheEntryReader.readFrom(gis)
        }
        logger.log(LOG_TAG, cacheKey.hashCode, "cache LOADED")
        return true
    }


    override fun close() {}
}