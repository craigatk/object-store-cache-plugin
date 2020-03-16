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
}
