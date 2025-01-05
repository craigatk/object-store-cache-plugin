package com.atkinsondev.cache.testkit

import com.atkinsondev.cache.testkit.util.*
import io.minio.GetBucketLifecycleArgs
import io.minio.messages.LifecycleConfiguration
import io.minio.messages.LifecycleRule
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
        LifecycleConfiguration bucketLifecycle = minioClient.getBucketLifecycle(GetBucketLifecycleArgs.builder().bucket(bucketName).build())

        then:
        bucketLifecycle.rules().size() == 1

        LifecycleRule rule = bucketLifecycle.rules()[0]
        rule.expiration().days() == 30
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
        LifecycleConfiguration bucketLifecycle = minioClient.getBucketLifecycle(GetBucketLifecycleArgs.builder().bucket(bucketName).build())

        then:
        bucketLifecycle == null
    }
}
