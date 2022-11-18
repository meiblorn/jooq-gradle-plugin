package io.github.meiblorn.jooq.settings

import io.github.meiblorn.jooq.settings.JooqDockerPluginSettings.WithContainer
import io.github.meiblorn.jooq.settings.JooqDockerPluginSettings.WithoutContainer
import kotlin.streams.asStream
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expect
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class JooqDockerPluginSettingsTest {

    @TestFactory
    fun `JooqDockerPluginSettings implementations should implement equals and hashcode`() =
        sequenceOf(
                WithContainer().let {
                    Triple(
                        it,
                        it.copy(),
                        WithContainer().apply {
                            database.apply { name = "dbname" }
                            image.apply { name = "imagename" }
                        },
                    )
                },
                WithoutContainer().let {
                    Triple(
                        it,
                        it.copy(),
                        WithoutContainer().apply { database.apply { name = "dbname" } },
                    )
                })
            .map { (instance, instanceCopy, differentInstance) ->
                dynamicTest("${instance::class.simpleName} should implement equals and hashcode") {
                    expect {
                        @Suppress("KotlinConstantConditions") that(instance == instance).isTrue()
                        that(instance == instanceCopy).isTrue()
                        that(instance == differentInstance).isFalse()

                        that(instance.hashCode() == instance.hashCode()).isTrue()
                        that(instance.hashCode() == instanceCopy.hashCode()).isTrue()
                        that(instance.hashCode() == differentInstance.hashCode()).isFalse()
                    }
                }
            }
            .asStream()
}
