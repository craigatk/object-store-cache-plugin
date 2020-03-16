package com.atkinsondev.cache.client

import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.util.concurrent.PollingConditions

class ObjectStoreClientSpec extends Specification {

    @Subject
    ObjectStoreClient objectStoreClient = new ObjectStoreClient(
            "http://localhost:9000",
            "minio_access_key",
            "minio_secret_key",
            null
    )

    def "should create bucket that does not exist"() {
        given:
        String bucketName = "newbucket${RandomStringUtils.randomAlphabetic(8).toLowerCase()}"

        when:
        objectStoreClient.createBucketIfNotExists(bucketName)

        then:
        new PollingConditions().eventually {
            assert objectStoreClient.bucketExists(bucketName)
        }
    }

    def "should put then get object"() {
        given:
        File objectFile = new File(getClass().getResource("/cache1.txt").toURI())
        assert objectFile.exists()

        byte[] bytes = objectFile.bytes

        String bucketName = "putgetbucket"
        objectStoreClient.createBucketIfNotExists(bucketName)

        when:
        objectStoreClient.putObject(bucketName, "cache1.txt", bytes.size(), new ByteArrayInputStream(bytes))

        InputStream resultingStream = objectStoreClient.getObject(bucketName, "cache1.txt")

        then:
        resultingStream != null

        resultingStream.text == "Here is a file to cache 1"
    }
}
