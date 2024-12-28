package com.atkinsondev.cache.testkit.util

import org.junit.rules.TemporaryFolder

class BuildFileWriter {
    static File writeBuildFile(TemporaryFolder projectRootDir) {
        File buildFile = projectRootDir.newFile('build.gradle')
        buildFile << """
            buildscript {
                repositories {
                    jcenter()
                }
            }

            plugins {
                id 'groovy'
            }
            
            repositories {
                jcenter()
            }
            
            dependencies {
                implementation('org.codehaus.groovy:groovy-all:3.0.8')

                testImplementation('org.spockframework:spock-core:2.3-groovy-3.0')
            }
        """.stripIndent()

        return buildFile
    }
}
