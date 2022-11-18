package io.github.meiblorn.jooq

import io.github.meiblorn.jooq.util.DependencyVersionsExtractor

object RecommendedVersions {

    @JvmStatic private val versionExtractor by lazy { DependencyVersionsExtractor() }

    @JvmStatic val JOOQ_VERSION by versionExtractor

    @JvmStatic val FLYWAY_VERSION by versionExtractor
}
