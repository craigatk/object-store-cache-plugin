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

        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                endpoint,
                bucketName,
                accessKey,
                secretKey
        )

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        println compileGroovyResult.output

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS

        when:
        def testExecutedResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('clean', 'test', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        println testExecutedResult

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
