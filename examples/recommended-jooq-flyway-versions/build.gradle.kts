/**
 * This is how you can configure jOOQ and Flyway dependency versions aligned with the plugin's built-in versions
 */

import io.github.meiblorn.jooq.RecommendedVersions

plugins {
    kotlin("jvm") version "1.6.21"
    id("io.github.meiblorn.jooq-docker") version "1.3.8"
}

repositories {
    mavenCentral()
}

dependencies {
    jooqCodegen("org.postgresql:postgresql:42.3.6")
    implementation("org.postgresql:postgresql:42.3.6")
    implementation("org.jooq:jooq:${RecommendedVersions.JOOQ_VERSION}")
    implementation("org.flywaydb:flyway-core:${RecommendedVersions.FLYWAY_VERSION}")
}
