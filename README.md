# Object store Gradle build cache plugin

Plugin to use an S3-compatible object store as the backend for a Gradle remote [build cache](https://docs.gradle.org/current/userguide/build_cache.html).

The plugin uses the [Minio Java SDK](https://github.com/minio/minio-java) to communicate to the object store so it should work with
any S3-compatible object store like Amazon S3, Digital Ocean Spaces, etc. I use it with Digital Ocean Spaces,
but if folks use the plugin with other object stores please report back successes or issues and we can
update the documentation / work through issues accordingly.

## Usage

### Apply plugin in settings.gradle

First, apply the plugin in `settings.gradle`

```
plugins {
    id "com.atkinsondev.object-store-cache" version "3.0.0"
}
```

For the current version, please see https://plugins.gradle.org/plugin/com.atkinsondev.object-store-cache

### Configure build cache

Then configure Gradle's build caches and add the object store remote cache in `settings.gradle`

```
// Use whichever environment variable is set by your CI system
boolean isCI = Boolean.valueOf(System.getenv("GITHUB_ACTIONS"))

String cacheAccessKey = System.getenv("CACHE_ACCESS_KEY") ?: properties['cache_access_key']
String cacheSecretKey = System.getenv("CACHE_SECRET_KEY") ?: properties['cache_secret_key']

buildCache {
    local {
        enabled = !isCI
    }
    remote(com.atkinsondev.cache.ObjectStoreBuildCache) {
        endpoint = '<endpoint>'
        accessKey = cacheAccessKey
        secretKey = cacheSecretKey
        bucket = '<bucket_name>'
        push = isCI
        enabled = cacheAccessKey && cacheSecretKey
    }
}
```

As an example, this plugin's build uses the build cache plugin itself: https://github.com/craigatk/object-store-cache-plugin/blob/master/settings.gradle

#### Local credentials

Then you can pass in the access key and secret key either with those environment variables
or store them in `~/.gradle/gradle.properties` outside source control with those property names.

```
cache_access_key=<your_access_key>
cache_secret_key=<your_secret_key>
```

### Configuration properties

All the plugin configuration properties:

| Parameter        | Type      | Required | Default | Description                                                                                |
|------------------|-----------|----------|---------|--------------------------------------------------------------------------------------------|
| endpoint         | `String`  | yes      |         | Endpoint of S3-compatible backend                                                          |  
| accessKey        | `String`  | yes      |         | Object store access key                                                                    |
| secretKey        | `String`  | yes      |         | Object store secret key                                                                    |
| bucket           | `String`  | yes      |         | Bucket name to store the cache files                                                       |
| autoCreateBucket | `boolean` | no       | `false` | Whether to automatically create the bucket if it does not exist                            |
| expirationInDays | `int`     | no       |         | Configure the bucket's lifecycle policy to expire objects when they are this many days old |
| region           | `String`  | no       | `null`  | Region where the bucket resides (if supported by backend)                                  |          

## Compatibility

The plugin is compatible with Gradle `8.4` and higher and Java `21` and higher.

## Changelog

* 3.0.0
  * Plugin now requires Java 21+
  * Built with Gradle 9
  * Missing cache access key or secret key now fail build (required due to API changes in Gradle 9). Add `enabled = cacheAccessKey && cacheSecretKey` to the plugin Gradle config to disable the remote cache if those fields are not available.  
* 2.1.0
  * Upgrading to Minio 8
  * Plugin now requires Gradle 8.4+
* 2.0.0
  * BREAKING CHANGE: Plugin now requires Java 17+ and Gradle 8+
* 1.4.0
  * Upgrading Kotlin dependency to 1.7.21
* 1.3.1
  * Upgrading Kotlin dependency to 1.6.20 and build to Gradle 7.5.1
* 1.2.0
  * Switching from jCenter to Maven Central
* 1.1.2
  * Improving error message when access or secret key is missing
* 1.0.0
  * Initial release