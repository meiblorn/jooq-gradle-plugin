package io.github.meiblorn.jooq.util

import java.net.URLClassLoader
import org.gradle.api.file.FileCollection

internal class CodegenClasspathAwareClassLoaders(
    val buildscriptExclusive: URLClassLoader,
    val buildscriptInclusive: URLClassLoader,
) {
    companion object {

        fun from(classpath: FileCollection) =
            classpath
                .map { it.toURI().toURL() }
                .toTypedArray()
                .let {
                    CodegenClasspathAwareClassLoaders(
                        buildscriptExclusive = URLClassLoader(it),
                        buildscriptInclusive =
                            URLClassLoader(
                                it, CodegenClasspathAwareClassLoaders::class.java.classLoader))
                }
    }
}
