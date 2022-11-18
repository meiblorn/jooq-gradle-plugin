package io.github.meiblorn.jooq.migration

import io.github.meiblorn.jooq.settings.DatabaseCredentials

internal interface MigrationRunner {
    fun migrateDb(
        schemas: Array<String>,
        migrationLocations: Array<String>,
        flywayProperties: Map<String, String>,
        credentials: DatabaseCredentials,
        defaultFlywaySchema: String,
        flywayTable: String,
    ): SchemaVersion
}
