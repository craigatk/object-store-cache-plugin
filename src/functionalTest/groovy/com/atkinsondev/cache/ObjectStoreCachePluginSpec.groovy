package com.atkinsondev.cache

import com.atkinsondev.cache.util.BuildFileWriter
import com.atkinsondev.cache.util.PluginClasspathData
import com.atkinsondev.cache.util.PluginClasspathLoader
import com.atkinsondev.cache.util.SettingsFileWriter
import com.atkinsondev.cache.util.SourceFileWriter
import org.apache.commons.lang3.RandomStringUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ObjectStoreCachePluginSpec extends Specification {
    @Rule
    TemporaryFolder projectDir = new TemporaryFolder()

    PluginClasspathData pluginClasspathData

    File settingsFile

    String bucketName = "testbucket"

    def setup() {
        BuildFileWriter.writeBuildFile(projectDir)

        pluginClasspathData = new PluginClasspathLoader().loadPluginClasspath()

        settingsFile = SettingsFileWriter.writeSettingsFile(projectDir, pluginClasspathData.pluginClasspath)
    }

    def "when re-using cached source class files in second build with tests should work"() {
        given:
        File sourceDirectory = SourceFileWriter.createSourceDirectory(projectDir)
        File testDirectory = SourceFileWriter.createTestDirectory(projectDir)

        String randomPieceOfClassNameForUniqueness = RandomStringUtils.randomAlphabetic(12)
        String sourceClassName = "Sample${randomPieceOfClassNameForUniqueness}"

        SourceFileWriter.writeSourceFile(sourceDirectory, sourceClassName)
        SourceFileWriter.writeSpecFile(testDirectory, sourceClassName)

        settingsFile << """
        buildCache {
            local {
                enabled = false
            }
            remote(com.atkinsondev.cache.ObjectStoreBuildCache) {
                endpoint = 'http://localhost:9000'
                accessKey = 'minio_access_key'
                secretKey = 'minio_secret_key'
                bucket = '${bucketName}'
                autoCreateBucket = true
                push = true
            }
        }
        """.stripIndent()

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS

        when:
        def testExecutedResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('clean', 'test', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        then:
        testExecutedResult.task(":compileGroovy").outcome == TaskOutcome.FROM_CACHE
        testExecutedResult.task(":test").outcome == TaskOutcome.SUCCESS

        when:
        def testFromCacheResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('clean', 'test', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        then:
        testFromCacheResult.task(":compileGroovy").outcome == TaskOutcome.FROM_CACHE
        testFromCacheResult.task(":test").outcome == TaskOutcome.FROM_CACHE
    }
}
