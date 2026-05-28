package dev.victorlpgazolli.service

import dev.victorlpgazolli.DecentralizedConfiguration
import dev.victorlpgazolli.client.CacheManifest
import dev.victorlpgazolli.client.IpfsClient
import dev.victorlpgazolli.utils.QuietLogger
import dev.victorlpgazolli.utils.SimpleLogger
import org.gradle.caching.BuildCacheService
import org.gradle.caching.BuildCacheServiceFactory

private const val LOG_TAG = "[decentralized-cache]"
internal class IpfsBuildCacheServiceFactory : BuildCacheServiceFactory<DecentralizedConfiguration> {
    override fun createBuildCacheService(
        decentralizedConfiguration: DecentralizedConfiguration,
        describer: BuildCacheServiceFactory.Describer,
    ): BuildCacheService {

        val logger = if(decentralizedConfiguration.verbose) {
            SimpleLogger()
        } else {
            QuietLogger()
        }

        logger.log(LOG_TAG, "IpfsBuildCacheServiceFactory", "creating build cache service")

        val ipfsClient = IpfsClient(
            configuration = decentralizedConfiguration,
            cacheManifest = CacheManifest(logger),
            logger = logger
        )

        logger.log(LOG_TAG, "IpfsBuildCacheServiceFactory", "host=${ipfsClient.hostBaseUrl ?: "default"}; version=${ipfsClient.version}")

        return IpfsBuildCacheService(
            ipfsClient = ipfsClient,
            logger = logger
        )
    }
}