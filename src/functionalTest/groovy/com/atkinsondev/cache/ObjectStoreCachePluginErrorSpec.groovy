package com.atkinsondev.cache

import com.atkinsondev.cache.service.ObjectStoreBuildCacheServiceFactory
import com.atkinsondev.cache.util.SettingsFileWriter
import com.atkinsondev.cache.util.SourceFileWriter
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

class ObjectStoreCachePluginErrorSpec extends ObjectStoreCachePluginSpecCase {
    String bucketName = "errorbucket"

    def "when connecting to object store host fails should disable cache and not fail build"() {
        given:
        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                'http://invalidhostname',
                bucketName,
                accessKey,
                secretKey
        )

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache', '--stacktrace')
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
        testExecutedResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS
        testExecutedResult.task(":test").outcome == TaskOutcome.SUCCESS
    }

    @Unroll
    def "when access key is #settingsAccessKey and secret key is #settingsSecretKey should disable build cache"() {
        given:
        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                'http://invalidhostname',
                bucketName,
                settingsAccessKey,
                settingsSecretKey
        )

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache', '--stacktrace')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        println compileGroovyResult.output

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS

        and:
        compileGroovyResult.output.contains(ObjectStoreBuildCacheServiceFactory.missingKeysErrorMessage)

        when:
        def testExecutedResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('clean', 'test', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        then:
        testExecutedResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS
        testExecutedResult.task(":test").outcome == TaskOutcome.SUCCESS

        where:
        settingsAccessKey | settingsSecretKey
        null              | secretKey
        accessKey         | null
        null              | null
    }
}
