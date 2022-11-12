package com.atkinsondev.cache.testkit.util

// Getting the plugin files onto the classpath for a Settings plugin requires some trickery,
// big thanks to GitHub user boazj, who figured out a way - https://github.com/boazj/gradle-testkit-project-dir-issue
class PluginClasspathLoader {
    PluginClasspathData loadPluginClasspath() {
        File pluginClasspathFile = new File('build/createClasspathManifest/plugin-classpath.txt')
        if (!pluginClasspathFile?.exists()) {
            throw new IllegalStateException('Did not find plugin classpath resource, run `testClasses` build task.')
        }

        List<File> pluginClasspathFiles = pluginClasspathFile.readLines()
                .collect { it.replace("\\", '\\\\') }
                .collect { new File(it) }

        List<String> pluginClasspath = pluginClasspathFile.readLines()
                .collect { it.replace('\\', '\\\\') }
                .collect { "\'${it}\'" }

        return new PluginClasspathData(pluginClasspathFiles: pluginClasspathFiles, pluginClasspath: pluginClasspath)
    }
}

class PluginClasspathData {
    List<File> pluginClasspathFiles
    List<String> pluginClasspath
}
