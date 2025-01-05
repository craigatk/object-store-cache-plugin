package com.atkinsondev.cache.testkit

import com.atkinsondev.cache.testkit.util.BuildFileWriter
import com.atkinsondev.cache.testkit.util.PluginClasspathData
import com.atkinsondev.cache.testkit.util.PluginClasspathLoader
import com.atkinsondev.cache.testkit.util.SettingsFileWriter
import com.atkinsondev.cache.testkit.util.SourceFileWriter
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

    MinioClient minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build()

    def setup() {
        BuildFileWriter.writeBuildFile(projectDir)

        pluginClasspathData = new PluginClasspathLoader().loadPluginClasspath()

        settingsFile = SettingsFileWriter.writeSettingsFile(projectDir, pluginClasspathData.pluginClasspath)

        sourceDirectory = SourceFileWriter.createSourceDirectory(projectDir)
        testDirectory = SourceFileWriter.createTestDirectory(projectDir)
    }
}
