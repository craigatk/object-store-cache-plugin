package com.atkinsondev.cache.client

class BucketMissingException(
    bucket: String,
) : RuntimeException("Bucket does not exist $bucket")
