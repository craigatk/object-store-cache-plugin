package com.atkinsondev.cache

import com.atkinsondev.cache.util.SettingsFileWriter
import com.atkinsondev.cache.util.SourceFileWriter
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class ObjectStoreCachePluginSpec extends ObjectStoreCachePluginSpecCase {
    String bucketName = "testbucket"

    def "when re-using cached source class files in second build with tests should work"() {
        given:
        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        SettingsFileWriter.writeBuildCacheConfig(settingsFile, endpoint, bucketName)

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

    def "when connecting to object store host fails should disable cache and not fail build"() {
        given:
        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        SettingsFileWriter.writeBuildCacheConfig(settingsFile, 'http://invalidhostname', bucketName)

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
}
