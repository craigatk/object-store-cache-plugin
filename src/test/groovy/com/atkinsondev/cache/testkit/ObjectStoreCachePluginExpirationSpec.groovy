package com.atkinsondev.cache.testkit

import com.atkinsondev.cache.testkit.util.*
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

class ObjectStoreCachePluginExpirationSpec extends ObjectStoreCachePluginSpecCase {

    def "when expiration specified should set expiration date"() {
        given:
        String bucketName = "expirationbucket"

        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                endpoint,
                bucketName,
                accessKey,
                secretKey,
                "expirationInDays = 30"
        )

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
        String bucketLifecycle = minioClient.getBucketLifeCycle(bucketName)

        then:
        bucketLifecycle.contains("<Expiration><Days>30</Days></Expiration>")
    }

    def "when no expiration specified should not set expiration date"() {
        given:
        String bucketName = "noexpirationbucket"

        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                endpoint,
                bucketName,
                accessKey,
                secretKey,
                ""
        )

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS

        when:
        String bucketLifecycle = minioClient.getBucketLifeCycle(bucketName)

        then:
        bucketLifecycle.isEmpty()
    }
}
