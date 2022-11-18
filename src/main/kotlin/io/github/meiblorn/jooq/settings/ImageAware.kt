package io.github.meiblorn.jooq.settings

import groovy.lang.Closure
import io.github.meiblorn.jooq.util.callWith
import org.gradle.api.Action

internal interface ImageAware {
    /** Configure the Docker image settings. */
    fun image(customizer: Action<Image>)

    /** Configure the Docker image settings. */
    fun image(closure: Closure<Image>) = image(closure::callWith)
}
