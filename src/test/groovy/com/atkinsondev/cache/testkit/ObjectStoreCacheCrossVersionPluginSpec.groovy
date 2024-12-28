package com.atkinsondev.cache.testkit


import com.atkinsondev.cache.testkit.util.SettingsFileWriter
import com.atkinsondev.cache.testkit.util.SourceFileWriter
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import spock.lang.Unroll

class ObjectStoreCacheCrossVersionPluginSpec extends ObjectStoreCachePluginSpecCase {
    private String bucketName = "cross-version-bucket"

    @Unroll
    def "when re-using cached source class files in second build with tests should work with Gradle version #gradleVersion"() {
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
                .withGradleVersion(gradleVersion)
                .build()

        println compileGroovyResult.output

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS

        when:
        def testExecutedResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('clean', 'test', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .withGradleVersion(gradleVersion)
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
                .withGradleVersion(gradleVersion)
                .build()

        then:
        testFromCacheResult.task(":compileGroovy").outcome == TaskOutcome.FROM_CACHE
        testFromCacheResult.task(":test").outcome == TaskOutcome.FROM_CACHE

        where:
        gradleVersion                   || _
        "8.0"                           || _
        "8.4"                           || _
        GradleVersion.current().version || _
    }
}
