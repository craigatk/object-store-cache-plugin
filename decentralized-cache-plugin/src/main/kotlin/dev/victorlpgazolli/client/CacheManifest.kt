@file:OptIn(ExperimentalSerializationApi::class)

package dev.victorlpgazolli.client

import dev.victorlpgazolli.utils.Logger
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.io.File


object CacheManifestResultSerializer : JsonContentPolymorphicSerializer<CacheManifestResult>(CacheManifestResult::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CacheManifestResult> {
        val jsonObject = element.jsonObject
        return when {
            "Message" in jsonObject -> CacheManifestResult.Failure.serializer()
            else -> CacheManifestResult.Success.serializer()
        }
    }
}
@Serializable(with = CacheManifestResultSerializer::class)
sealed class CacheManifestResult {

    @Serializable
    data class Failure(
        @SerialName("Message") val message: String,
        @SerialName("Code") val code: Int,
        @SerialName("Type") val type: String
    ): CacheManifestResult()
    @Serializable
    data class Success(
        val publishKeyName: String,
        val hashs: Map<String, String>,
    ): CacheManifestResult()
}
typealias Manifest = CacheManifestResult.Success

internal class CacheManifest(
    val logger: Logger
){

    public fun setup(client: IpfsClient) {
        this.client = client
        this.manifest = fetch()
    }

    private lateinit var client: IpfsClient
    private lateinit var manifest: Manifest

    private val manifestFileName = "manifest.json"
    private val emptyManifest = Manifest(
        publishKeyName = "cache",
        hashs = emptyMap(),
    )

    public fun translateToIpfsHash(objectName: String): String? {
        return if(this::manifest.isInitialized) {
            logger.log(LOG_TAG, "translateToIpfsHash", "ipfs for $objectName hash is ${manifest.hashs[objectName]}")
            manifest.hashs[objectName]
        } else { null }
    }
    private fun fetch(): Manifest {
        val manifestIpnsPath = "${client.baseIpns}/$manifestFileName"

        logger.log(LOG_TAG, "fetch", "fetching local manifest at $manifestFileName")
        client.mfs.read(manifestFileName)
            ?.readAllBytes()
            ?.decodeToString()
            ?.decodeManifest()
            ?.let {
                logger.log(LOG_TAG, "fetch", "found local manifest: $it")
                return it
            }

        logger.log(LOG_TAG, "fetch", "fetching remote manifest at $manifestIpnsPath")
        client.getObject(manifestIpnsPath)
            ?.readAllBytes()
            ?.decodeToString()
            ?.decodeManifest()
            ?.let {
                logger.log(LOG_TAG, "fetch", "found remote manifest: $it")
                return it
            }

        return saveEmptyManifest()
    }

    private fun saveEmptyManifest(): Manifest {
        logger.log(LOG_TAG, "saveEmptyManifest", "manifest is empty")

        val tmpManifestPath = "/tmp/local-ipfs-gradle-cache"

        File(tmpManifestPath).run {
            delete()
            mkdir()
        }
        val newManifestFile = File("$tmpManifestPath/$manifestFileName")

        newManifestFile.createNewFile()
        newManifestFile.writeText(emptyManifest.encodeManifest())
        logger.log(LOG_TAG, "saveEmptyManifest", "created empty manifest at $tmpManifestPath")

        client.putObject(
            filePath = newManifestFile.absolutePath,
            objectName = manifestFileName,
        )

        return emptyManifest
    }
    val jsonParser = Json {
        ignoreUnknownKeys = true
    }

    private fun String.decodeManifest(): Manifest {
        runCatching {
            logger.log(LOG_TAG, "decodeManifest", "decoding manifest: $this")

            return when(val manifestResult = jsonParser.decodeFromString<CacheManifestResult>(this)) {
                is CacheManifestResult.Failure -> {
                    logger.log(LOG_TAG, "decodeManifest", "manifest failed: ${manifestResult.message}")
                    emptyManifest
                }
                is CacheManifestResult.Success -> {
                    logger.log(LOG_TAG, "decodeManifest", "manifest success: $manifestResult")
                    manifestResult
                }
            }
        }.onFailure {
            logger.log(LOG_TAG, "decodeManifest", "failed to decode manifest $this; cause -> ${it.message}")
        }.onSuccess {
            logger.log(LOG_TAG, "decodeManifest", "decoded manifest: $it")
        }
        return emptyManifest
    }
    private fun Manifest.encodeManifest(): String {
        return Json.encodeToString(Manifest.serializer(), this)
    }
    companion object {
        private const val LOG_TAG = "[decentralized-cache]"
    }
}
