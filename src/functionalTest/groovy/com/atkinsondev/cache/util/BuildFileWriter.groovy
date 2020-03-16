package com.atkinsondev.cache.util

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
                implementation('org.codehaus.groovy:groovy-all:2.5.10')

                testImplementation('org.spockframework:spock-core:1.3-groovy-2.5')
            }
        """.stripIndent()

        return buildFile
    }
}
