package com.atkinsondev.cache.service

import com.atkinsondev.cache.ObjectStoreBuildCache
import com.atkinsondev.cache.client.BucketMissingException
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.caching.BuildCacheServiceFactory
import spock.lang.Specification
import spock.lang.Subject

class ObjectStoreBuildCacheServiceFactorySpec extends Specification {

    @Subject
    ObjectStoreBuildCacheServiceFactory objectStoreBuildCacheServiceFactory = new ObjectStoreBuildCacheServiceFactory()

    def "when bucket does not exist and auto-bucket-create disabled should throw exception"() {
        given:
        String bucketNameThatDoesNotExist = "bucket${RandomStringUtils.randomAlphabetic(8).toLowerCase()}"

        ObjectStoreBuildCache objectStoreBuildCache = new ObjectStoreBuildCache()
        objectStoreBuildCache.endpoint = "http://localhost:9000"
        objectStoreBuildCache.accessKey = "minio_access_key"
        objectStoreBuildCache.secretKey = "minio_secret_key"
        objectStoreBuildCache.bucket = bucketNameThatDoesNotExist
        objectStoreBuildCache.autoCreateBucket = false

        when:
        objectStoreBuildCacheServiceFactory.createBuildCacheService(objectStoreBuildCache, Mock(BuildCacheServiceFactory.Describer))

        then:
        thrown(BucketMissingException)
    }
}
