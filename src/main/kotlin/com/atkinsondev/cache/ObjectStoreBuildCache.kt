package com.atkinsondev.cache

import org.gradle.caching.configuration.AbstractBuildCache

open class ObjectStoreBuildCache : AbstractBuildCache() {
    lateinit var endpoint: String
    lateinit var accessKey: String
    lateinit var secretKey: String
    lateinit var bucket: String
    var autoCreateBucket: Boolean = false
    var region: String? = null
    var expirationInDays: Int? = null
}
