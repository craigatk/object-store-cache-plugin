package com.atkinsondev.cache

import org.gradle.caching.configuration.AbstractBuildCache

open class ObjectStoreBuildCache : AbstractBuildCache() {
    lateinit var endpoint: String
    var accessKey: String? = null
    var secretKey: String? = null
    lateinit var bucket: String
    var autoCreateBucket: Boolean = false
    var region: String? = null
    var expirationInDays: Int? = null
}
