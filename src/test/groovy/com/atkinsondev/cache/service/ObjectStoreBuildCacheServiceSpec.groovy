package com.atkinsondev.cache.service

import com.atkinsondev.cache.client.ObjectStoreClient
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.caching.BuildCacheKey
import org.gradle.caching.internal.controller.service.LoadTarget
import org.gradle.caching.internal.controller.service.StoreTarget
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

class ObjectStoreBuildCacheServiceSpec extends Specification {
    @Rule
    TemporaryFolder cacheDestinationDir = new TemporaryFolder()

    String bucketName = "objectstoretest"
    ObjectStoreClient objectStoreClient = new ObjectStoreClient(
            "http://localhost:9000",
            "minio_access_key",
            "minio_secret_key",
            null
    )

    @Subject
    ObjectStoreBuildCacheService objectStoreBuildCacheService = new ObjectStoreBuildCacheService(bucketName, objectStoreClient)

    def setup() {
        objectStoreClient.createBucketIfNotExists(bucketName)
    }

    def "should write then read file from cache"() {
        given:
        File fileToCache = new File(getClass().getResource("/cache1.txt").toURI())
        assert fileToCache.exists()

        File cacheFileDestination = cacheDestinationDir.newFile("cache1-loaded.txt")

        String hashCode = "hashcode${RandomStringUtils.randomAlphabetic(16)}"
        BuildCacheKey buildCacheKey = createBuildCacheKey(hashCode)

        when:
        objectStoreBuildCacheService.store(buildCacheKey, new StoreTarget(fileToCache))

        and:
        boolean loadSuccessful = objectStoreBuildCacheService.load(buildCacheKey, new LoadTarget(cacheFileDestination))

        then:
        loadSuccessful == true

        and:
        cacheFileDestination.text == fileToCache.text
    }

    def "when build cache key does not exist should return false when trying to load it"() {
        given:
        File cacheFileDestination = cacheDestinationDir.newFile("cache1-loaded.txt")

        String hashCode = "hashcode${RandomStringUtils.randomAlphabetic(16)}"
        BuildCacheKey buildCacheKey = createBuildCacheKey(hashCode)

        when:
        boolean loadSuccessful = objectStoreBuildCacheService.load(buildCacheKey, new LoadTarget(cacheFileDestination))

        then:
        loadSuccessful == false
    }

    private static BuildCacheKey createBuildCacheKey(String hashCode) {
        return new BuildCacheKey() {
            @Override
            String getHashCode() {
                return hashCode
            }

            @Override
            byte[] toByteArray() {
                return new byte[0]
            }
        }
    }
}
