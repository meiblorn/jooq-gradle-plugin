package io.github.meiblorn.jooq.codegen

import org.jooq.meta.jaxb.Configuration

internal interface JooqCodegenRunner {
    fun generateJooqClasses(configuration: Configuration)
}
