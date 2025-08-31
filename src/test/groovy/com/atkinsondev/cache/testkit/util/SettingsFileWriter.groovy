package com.atkinsondev.cache.testkit.util

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

    static void writeBuildCacheConfig(
            File settingsFile,
            String endpoint,
            String bucketName,
            String accessKey,
            String secretKey,
            String additionalConfig = ""
    ) {
        String accessKeyContents = accessKey ? "'${accessKey}'" : "null"
        String secretKeyContents = secretKey ? "'${secretKey}'" : "null"

        settingsFile << """
        buildCache {
            local {
                enabled = false
            }
            remote(com.atkinsondev.cache.ObjectStoreBuildCache) {
                endpoint = '${endpoint}'
                accessKey = ${accessKeyContents}
                secretKey = ${secretKeyContents}
                bucket = '${bucketName}'
                autoCreateBucket = true
                push = true
                ${additionalConfig}
                enabled = accessKey && secretKey
            }
        }
        """.stripIndent()
    }
}
