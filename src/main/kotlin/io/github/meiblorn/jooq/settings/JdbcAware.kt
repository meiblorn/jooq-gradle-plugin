package io.github.meiblorn.jooq.settings

import groovy.lang.Closure
import io.github.meiblorn.jooq.util.callWith
import org.gradle.api.Action

internal interface JdbcAware {
    /** Configures the JDBC connection settings. */
    fun jdbc(customizer: Action<Jdbc>)

    /** Configures the JDBC connection settings. */
    fun jdbc(closure: Closure<Jdbc>) = jdbc(closure::callWith)
}
