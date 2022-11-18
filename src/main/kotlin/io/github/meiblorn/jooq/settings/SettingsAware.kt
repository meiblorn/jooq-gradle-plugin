package io.github.meiblorn.jooq.settings

import groovy.lang.Closure
import io.github.meiblorn.jooq.settings.JooqDockerPluginSettings.WithContainer
import io.github.meiblorn.jooq.settings.JooqDockerPluginSettings.WithoutContainer
import io.github.meiblorn.jooq.util.callWith
import org.gradle.api.Action

internal interface SettingsAware {
    /** Configures the Jooq Docker plugin to run with a DB container. */
    @Suppress("unused") fun withContainer(configure: Action<WithContainer>)

    /** Configures the Jooq Docker plugin to run with a DB container. */
    @Suppress("unused")
    fun withContainer(closure: Closure<WithContainer>) = withContainer(closure::callWith)

    /**
     * Configures the Jooq Docker plugin to run without a DB container (using an external DB
     * instance).
     */
    @Suppress("unused") fun withoutContainer(configure: Action<WithoutContainer>)

    /**
     * Configures the Jooq Docker plugin to run without a DB container (using an external DB
     * instance).
     */
    @Suppress("unused")
    fun withoutContainer(closure: Closure<WithoutContainer>) = withoutContainer(closure::callWith)
}
