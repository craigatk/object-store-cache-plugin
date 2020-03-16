package com.atkinsondev.cache.client

import io.minio.MinioClient
import io.minio.errors.ErrorResponseException
import java.io.InputStream

class ObjectStoreClient(endpoint: String, accessKey: String, secretKey: String, private val region: String? = null) {
    private val minioClient = MinioClient(endpoint, accessKey, secretKey, region)

    fun createBucketIfNotExists(bucketName: String) {
        if (!bucketExists(bucketName)) {
            try {
                minioClient.makeBucket(bucketName, region)
            } catch (e: ErrorResponseException) {
                throw BucketCreationException("Error creating bucket", e)
            }
        }
    }

    fun bucketExists(bucketName: String) = minioClient.bucketExists(bucketName)

    fun putObject(bucketName: String, objectName: String, size: Long, stream: InputStream) {
        minioClient.putObject(
                bucketName,
                objectName,
                stream,
                size,
                null,
                null,
                "application/octet-stream"
        )
    }

    fun getObject(bucketName: String, objectName: String): InputStream? =
            try {
                minioClient.getObject(bucketName, objectName)
            } catch (e: ErrorResponseException) {
                null
            }
}
