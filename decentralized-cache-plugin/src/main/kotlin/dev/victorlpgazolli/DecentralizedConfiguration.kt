package dev.victorlpgazolli

import org.gradle.caching.configuration.AbstractBuildCache


open class DecentralizedConfiguration: AbstractBuildCache() {
    val hostBaseUrl: String? = null // if not defined 127.0.0.1:5001 will be used
    lateinit var baseIpns: String
    var verbose: Boolean = false
}

