package com.atkinsondev.cache.testkit.util

import org.junit.rules.TemporaryFolder

class BuildFileWriter {
    static File writeBuildFile(TemporaryFolder projectRootDir) {
        File buildFile = projectRootDir.newFile('build.gradle')
        buildFile << """
            buildscript {
                repositories {
                    mavenCentral()
                }
            }

            plugins {
                id 'groovy'
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation('org.apache.groovy:groovy-all:4.0.28')

                testImplementation('org.spockframework:spock-core:2.3-groovy-4.0')

                testImplementation 'org.junit.jupiter:junit-jupiter:5.9.2'
                testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
            }
            
            test {
                useJUnitPlatform()
            }
        """.stripIndent()

        return buildFile
    }
}
