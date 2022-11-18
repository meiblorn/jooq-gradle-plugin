package io.github.meiblorn.jooq.codegen

import io.github.meiblorn.jooq.JooqDockerPlugin.Companion.CONFIGURATION_NAME
import io.github.meiblorn.jooq.util.CodegenClasspathAwareClassLoaders
import org.jooq.meta.jaxb.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class UniversalJooqCodegenRunner {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun generateJooqClasses(
        codegenAwareClassLoader: CodegenClasspathAwareClassLoaders,
        configuration: Configuration
    ) {
        runCatching { ReflectiveJooqCodegenRunner(codegenAwareClassLoader.buildscriptExclusive) }
            .onFailure {
                logger.debug(
                    "Failed to load jOOQ code generation tool from $CONFIGURATION_NAME classpath",
                    it)
            }
            .onSuccess {
                logger.info("Loaded jOOQ code generation tool from $CONFIGURATION_NAME classpath")
            }
            .getOrElse {
                logger.info("Loaded jOOQ code generation tool from buildscript classpath")
                BuiltInJooqCodegenRunner(codegenAwareClassLoader.buildscriptInclusive)
            }
            .generateJooqClasses(configuration)
    }
}
