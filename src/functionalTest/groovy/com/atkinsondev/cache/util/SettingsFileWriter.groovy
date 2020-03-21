package com.atkinsondev.cache.util

import org.junit.rules.TemporaryFolder

class SettingsFileWriter {
    static File writeSettingsFile(TemporaryFolder projectRootDir, List<String> pluginClasspath) {
        File settingsFile = projectRootDir.newFile('settings.gradle')
        settingsFile << """
            buildscript {
                dependencies {
                    classpath files(${pluginClasspath})
                }
            }
            
            apply plugin: "com.atkinsondev.object-store-cache"
        """.stripIndent()

        return settingsFile
    }

    static void writeBuildCacheConfig(File settingsFile, String endpoint, String bucketName, String additionalConfig = "") {
        settingsFile << """
        buildCache {
            local {
                enabled = false
            }
            remote(com.atkinsondev.cache.ObjectStoreBuildCache) {
                endpoint = '${endpoint}'
                accessKey = 'minio_access_key'
                secretKey = 'minio_secret_key'
                bucket = '${bucketName}'
                autoCreateBucket = true
                push = true
                ${additionalConfig}
            }
        }
        """.stripIndent()
    }
}
