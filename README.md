# Object store Gradle build cache plugin

Plugin to use an S3-compatible object store as the backend for a Gradle remote build cache.

The plugin uses the [Minio Java SDK](https://github.com/minio/minio-java) to communicate to the object store so it should work with
any S3-compatible object store like Amazon S3, Digital Ocean Spaces, etc. I use it with Digital Ocean Spaces,
but if folks use the plugin with other object stores please report back successes or issues and we can
update the documentation / work through issues accordingly.

## Usage

### Apply plugin in settings.gradle

First, apply the plugin in `settings.gradle`

```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.atkinsondev.gradle:object-store-cache-plugin:<version>"
  }
}

apply plugin: apply plugin: "com.atkinsondev.object-store-cache"
```

### Configure build cache

Then configure Gradle's build caches and add the object store remote cache in `settings.gradle`

```
boolean isCI = Boolean.valueOf(System.getenv("GITHUB_ACTIONS"))

buildCache {
    local {
        enabled = !isCI
    }
    remote(com.atkinsondev.cache.ObjectStoreBuildCache) {
        endpoint = '<endpoint>'
        accessKey = '<access_key>'
        secretKey = '<secret_key>'
        bucket = '<bucket_name>'
        push = isCI
    }
}
```

### Configuration properties

All the plugin configuration properties:

| Parameter               | Type      | Required | Default | Description                                |
| ----------------------- | --------- | -------- | ------- | ------------------------------------------ |
| endpoint                | `String`  | yes      |         | Endpoint of S3-compatible backend |  
| accessKey               | `String`  | yes      |         | Object store access key |
| secretKey               | `String`  | yes      |         | Object store secret key |
| bucket                  | `String`  | yes      |         | Bucket name to store the cache files |
| autoCreateBucket        | `boolean` | no       | `false` | Whether to automatically create the bucket if it does not exist |
| region                  | `String`  | no       | `null`  | Region where the bucket resides (if supported by backend) |          
