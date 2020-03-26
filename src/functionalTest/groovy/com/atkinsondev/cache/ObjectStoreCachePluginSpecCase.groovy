package com.atkinsondev.cache

import com.atkinsondev.cache.util.BuildFileWriter
import com.atkinsondev.cache.util.PluginClasspathData
import com.atkinsondev.cache.util.PluginClasspathLoader
import com.atkinsondev.cache.util.SettingsFileWriter
import com.atkinsondev.cache.util.SourceFileWriter
import io.minio.MinioClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

class ObjectStoreCachePluginSpecCase extends Specification {
    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    PluginClasspathData pluginClasspathData

    File settingsFile

    File sourceDirectory
    File testDirectory

    @Shared
    String endpoint = 'http://localhost:9000'

    @Shared
    String accessKey = "minio_access_key"

    @Shared
    String secretKey = "minio_secret_key"

    MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey)

    def setup() {
        BuildFileWriter.writeBuildFile(projectDir)

        pluginClasspathData = new PluginClasspathLoader().loadPluginClasspath()

        settingsFile = SettingsFileWriter.writeSettingsFile(projectDir, pluginClasspathData.pluginClasspath)

        sourceDirectory = SourceFileWriter.createSourceDirectory(projectDir)
        testDirectory = SourceFileWriter.createTestDirectory(projectDir)
    }
}
