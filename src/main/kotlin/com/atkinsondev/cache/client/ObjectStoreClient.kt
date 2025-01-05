package com.atkinsondev.cache.client

import io.minio.*
import io.minio.errors.ErrorResponseException
import io.minio.messages.*
import java.io.InputStream

class ObjectStoreClient(endpoint: String, accessKey: String, secretKey: String, private val region: String? = null) {
    private val minioClient =
        MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .region(region)
            .build()

    fun createBucketIfNotExists(bucketName: String) {
        if (!bucketExists(bucketName)) {
            try {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).region(region).build())
            } catch (e: ErrorResponseException) {
                throw BucketCreationException("Error creating bucket", e)
            }
        }
    }

    fun bucketExists(bucketName: String) = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())

    fun putObject(
        bucketName: String,
        objectName: String,
        size: Long,
        stream: InputStream,
    ) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(stream, size, -1)
                .contentType("application/octet-stream")
                .build(),
        )
    }

    fun getObject(
        bucketName: String,
        objectName: String,
    ): InputStream? =
        try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build(),
            )
        } catch (e: ErrorResponseException) {
            null
        }

    fun setBucketExpiration(
        bucketName: String,
        expirationInDays: Int,
    ) {
        val expirationResponseData: ResponseDate? = null

        val expirationRule =
            LifecycleRule(
                Status.ENABLED,
                null,
                Expiration(expirationResponseData, expirationInDays, null),
                RuleFilter(null, "", null),
                "expire-bucket",
                null,
                null,
                null,
            )

        val config = LifecycleConfiguration(listOf(expirationRule))

        minioClient.setBucketLifecycle(SetBucketLifecycleArgs.builder().bucket(bucketName).config(config).build())
    }
}
