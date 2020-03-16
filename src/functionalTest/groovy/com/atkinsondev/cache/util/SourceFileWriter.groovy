package com.atkinsondev.cache.util

import org.junit.rules.TemporaryFolder

class SourceFileWriter {
    static File createSourceDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "main", "groovy", "cache")
    }

    static File writeSourceFile(File sourceDirectory, String sourceClassName) {
        File sourceFile = new File(sourceDirectory, "${sourceClassName}.groovy")

        sourceFile << """package cache

        class ${sourceClassName} {
            String foo() {
                return "bar"
            }
        }
        """.stripIndent()
    }

    static File createTestDirectory(TemporaryFolder projectDir) {
        projectDir.newFolder("src", "test", "groovy", "cache")
    }

    static File writeSpecFile(File testDirectory, String sourceClassName) {
        String specClassName = "${sourceClassName}Spec"
        File specFile = new File(testDirectory, "${specClassName}.groovy")

        specFile << """package cache

        import spock.lang.Specification
        
        class ${specClassName} extends Specification {
            void "sample test"() {
                expect:
                new ${sourceClassName}().foo() == "bar"
            }
        }
        """.stripIndent()
    }
}
