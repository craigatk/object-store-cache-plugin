package com.atkinsondev.cache.testkit

import com.atkinsondev.cache.testkit.util.SettingsFileWriter
import com.atkinsondev.cache.testkit.util.SourceFileWriter
import com.github.tomakehurst.wiremock.junit.WireMockRule
import io.minio.Xml
import io.minio.messages.LocationConstraint
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.head
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.Rule

import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class ObjectStoreCachePluginServerErrorSpec extends ObjectStoreCachePluginSpecCase {
    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    def "when loading data from server fails with HTTP error should not fail build"() {
        given:
        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                wireMockRule.baseUrl(),
                "my-bucket",
                "access-key",
                "settings-key"
        )


        wireMockRule.addStubMapping(
                get(urlEqualTo("/my-bucket?location="))
                        .willReturn(aResponse().withBody(Xml.marshal(new LocationConstraint())).withStatus(200))
                        .build())

        wireMockRule.addStubMapping(
                head(urlEqualTo("/my-bucket"))
                        .willReturn(aResponse().withStatus(200))
                        .build()
        )

        wireMockRule.addStubMapping(
                get(urlMatching("/my-bucket/.*"))
                        .willReturn(aResponse().withStatus(504))
                        .build()
        )

        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache', '--stacktrace')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        println compileGroovyResult.output

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS
    }

    def "when connection to bucket fails with HTTP error should not fail build"() {
        SettingsFileWriter.writeBuildCacheConfig(
                settingsFile,
                wireMockRule.baseUrl(),
                "my-bucket",
                "access-key",
                "settings-key"
        )


        wireMockRule.addStubMapping(
                get(urlEqualTo("/my-bucket?location="))
                        .willReturn(aResponse().withStatus(504))
                        .build())

        SourceFileWriter.writeSourceAndSpecFiles(sourceDirectory, testDirectory)

        when:
        def compileGroovyResult = GradleRunner.create()
                .withProjectDir(projectDir.root)
                .withArguments('compileGroovy', '--build-cache', '--stacktrace')
                .withPluginClasspath(pluginClasspathData.pluginClasspathFiles)
                .build()

        println compileGroovyResult.output

        then:
        compileGroovyResult.task(":compileGroovy").outcome == TaskOutcome.SUCCESS
    }
}
