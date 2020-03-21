package com.atkinsondev.cache

import com.atkinsondev.cache.util.BuildFileWriter
import com.atkinsondev.cache.util.PluginClasspathData
import com.atkinsondev.cache.util.PluginClasspathLoader
import com.atkinsondev.cache.util.SettingsFileWriter
import com.atkinsondev.cache.util.SourceFileWriter
import io.minio.MinioClient
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ObjectStoreCachePluginSpecCase extends Specification {
    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    PluginClasspathData pluginClasspathData

    File settingsFile

    File sourceDirectory
    File testDirectory

    String endpoint = 'http://localhost:9000'

    MinioClient minioClient = new MinioClient(endpoint, "minio_access_key", "minio_secret_key")

    def setup() {
        BuildFileWriter.writeBuildFile(projectDir)

        pluginClasspathData = new PluginClasspathLoader().loadPluginClasspath()

        settingsFile = SettingsFileWriter.writeSettingsFile(projectDir, pluginClasspathData.pluginClasspath)

        sourceDirectory = SourceFileWriter.createSourceDirectory(projectDir)
        testDirectory = SourceFileWriter.createTestDirectory(projectDir)
    }
}
