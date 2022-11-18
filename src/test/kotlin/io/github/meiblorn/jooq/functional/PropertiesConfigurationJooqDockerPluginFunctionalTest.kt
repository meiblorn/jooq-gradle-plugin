package io.github.meiblorn.jooq.functional

import io.github.meiblorn.jooq.container.PostgresContainer
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import strikt.api.expect
import strikt.assertions.isEqualTo
import strikt.java.exists

class PropertiesConfigurationJooqDockerPluginFunctionalTest : JooqDockerPluginFunctionalTestBase() {

    @Test
    fun `should ignore non-string properties`() {
        // given
        writeProjectFile("gradle.properties") {
            """
                io.github.meiblorn.jooq.nonStringProperty=value-to-override
            """.trimIndent()
        }
        prepareBuildGradleFile {
            """
                project.setProperty("io.github.meiblorn.jooq.nonStringProperty", 123)
                require(project.properties.get("io.github.meiblorn.jooq.nonStringProperty") !is String) {
                    "nonStringProperty should not be a string"
                }
                
                plugins {
                    id("io.github.meiblorn.jooq-docker")
                }                


                repositories {
                    mavenCentral()
                }

                dependencies {
                    jooqCodegen("org.postgresql:postgresql:42.3.6")
                }
            """.trimIndent()
        }
        copyResource(from = "/V01__init.sql", to = "src/main/resources/db/migration/V01__init.sql")

        // when
        val result = runGradleWithArguments("generateJooqClasses")

        // then
        expect {
            that(result).generateJooqClassesTask.outcome isEqualTo SUCCESS
            that(projectFile("build/generated-jooq/org/jooq/generated/tables/Foo.java")).exists()
        }
    }

    @Test
    fun `should support with container override to without container`() {
        // given
        val postgresContainer = PostgresContainer().also { it.start() }
        prepareBuildGradleFile {
            """
                plugins {
                    id("io.github.meiblorn.jooq-docker")
                }
                
                jooq {
                    withContainer {
                        image {
                            command = "postgres -p 6666"
                        }
                    }
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    jooqCodegen("org.postgresql:postgresql:42.3.6")
                }
            """.trimIndent()
        }
        copyResource(from = "/V01__init.sql", to = "src/main/resources/db/migration/V01__init.sql")

        // when
        val result =
            runGradleWithArguments(
                "generateJooqClasses",
                "-Pio.github.meiblorn.jooq.withoutContainer.db.username=${postgresContainer.username}",
                "-Pio.github.meiblorn.jooq.withoutContainer.db.password=${postgresContainer.password}",
                "-Pio.github.meiblorn.jooq.withoutContainer.db.name=${postgresContainer.databaseName}",
                "-Pio.github.meiblorn.jooq.withoutContainer.db.port=${postgresContainer.firstMappedPort}")
        postgresContainer.stop()

        // then
        expect {
            that(result).generateJooqClassesTask.outcome isEqualTo SUCCESS
            that(projectFile("build/generated-jooq/org/jooq/generated/tables/Foo.java")).exists()
        }
    }

    @Test
    fun `should support partial default configuration override via properties`() {
        // given
        prepareBuildGradleFile {
            """
                plugins {
                    id("io.github.meiblorn.jooq-docker")
                }
                
                jooq {
                    withContainer {
                        image {
                            command = "postgres -p 6666"
                        }
                    }
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    jooqCodegen("org.postgresql:postgresql:42.3.6")
                }
            """.trimIndent()
        }
        copyResource(from = "/V01__init.sql", to = "src/main/resources/db/migration/V01__init.sql")

        // when
        val result =
            runGradleWithArguments(
                "generateJooqClasses",
                "-Pio.github.meiblorn.jooq.withContainer.db.port=6666",
            )

        // then
        expect {
            that(result).generateJooqClassesTask.outcome isEqualTo SUCCESS
            that(projectFile("build/generated-jooq/org/jooq/generated/tables/Foo.java")).exists()
        }
    }

    @Test
    fun `should support customized configuration override via properties`() {
        // given
        prepareBuildGradleFile {
            """
                plugins {
                    id("io.github.meiblorn.jooq-docker")
                }
                
                jooq {
                    withContainer {
                        image {
                            command = "postgres -p 6666"
                        }
                    }
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    jooqCodegen("org.postgresql:postgresql:42.3.6")
                }
            """.trimIndent()
        }
        copyResource(from = "/V01__init.sql", to = "src/main/resources/db/migration/V01__init.sql")

        // when
        val result =
            runGradleWithArguments(
                "generateJooqClasses",
                "-Pio.github.meiblorn.jooq.withContainer.image.command=",
            )

        // then
        expect {
            that(result).generateJooqClassesTask.outcome isEqualTo SUCCESS
            that(projectFile("build/generated-jooq/org/jooq/generated/tables/Foo.java")).exists()
        }
    }

    @Test
    fun `should be possible to configure the plugin with properties to run with container`() {
        // given
        writeProjectFile("gradle.properties") {
            """
                io.github.meiblorn.jooq.withContainer.db.username=root
                io.github.meiblorn.jooq.withContainer.db.password=mysql
                io.github.meiblorn.jooq.withContainer.db.name=mysql
                io.github.meiblorn.jooq.withContainer.db.port=3306
                io.github.meiblorn.jooq.withContainer.db.jdbc.schema=jdbc:mysql
                io.github.meiblorn.jooq.withContainer.db.jdbc.driverClassName=com.mysql.cj.jdbc.Driver
                io.github.meiblorn.jooq.withContainer.db.jdbc.urlQueryParams=?useSSL=false
                io.github.meiblorn.jooq.withContainer.image.name=mysql:8.0.29
                io.github.meiblorn.jooq.withContainer.image.testQuery=SELECT 2
                io.github.meiblorn.jooq.withContainer.image.command=--default-authentication-plugin=mysql_native_password
                io.github.meiblorn.jooq.withContainer.image.envVars.MYSQL_ROOT_PASSWORD=mysql
                io.github.meiblorn.jooq.withContainer.image.envVars.MYSQL_DATABASE=mysql
            """.trimIndent()
        }
        prepareBuildGradleFile {
            """
                import io.github.meiblorn.jooq.RecommendedVersions
                
                plugins {
                    id("io.github.meiblorn.jooq-docker")
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    jooqCodegen("mysql:mysql-connector-java:8.0.29")
                    jooqCodegen("org.flywaydb:flyway-mysql:${'$'}{RecommendedVersions.FLYWAY_VERSION}")
                }
            """.trimIndent()
        }
        copyResource(
            from = "/V01__init_mysql.sql",
            to = "src/main/resources/db/migration/V01__init_mysql.sql")

        // when
        val result = runGradleWithArguments("generateJooqClasses")

        // then
        expect {
            that(result).generateJooqClassesTask.outcome isEqualTo SUCCESS
            that(projectFile("build/generated-jooq/org/jooq/generated/tables/Foo.java")).exists()
        }
    }

    @Test
    fun `should be possible to configure the plugin with properties to run without container`() {
        // given
        val postgresContainer = PostgresContainer().also { it.start() }
        writeProjectFile("gradle.properties") {
            """
                io.github.meiblorn.jooq.withoutContainer.db.username=${postgresContainer.username}
                io.github.meiblorn.jooq.withoutContainer.db.password=${postgresContainer.password}
                io.github.meiblorn.jooq.withoutContainer.db.name=${postgresContainer.databaseName}
                io.github.meiblorn.jooq.withoutContainer.db.port=${postgresContainer.firstMappedPort}
                io.github.meiblorn.jooq.withoutContainer.db.jdbc.schema=jdbc:postgresql
                io.github.meiblorn.jooq.withoutContainer.db.jdbc.driverClassName=org.postgresql.Driver
                io.github.meiblorn.jooq.withoutContainer.db.jdbc.urlQueryParams=?loggerLevel=OFF
            """.trimIndent()
        }
        prepareBuildGradleFile {
            """
                plugins {
                    id("io.github.meiblorn.jooq-docker")
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    jooqCodegen("org.postgresql:postgresql:42.3.6")
                }
            """.trimIndent()
        }
        copyResource(
            from = "/V01__init_mysql.sql",
            to = "src/main/resources/db/migration/V01__init_mysql.sql")

        // when
        val result = runGradleWithArguments("generateJooqClasses")
        postgresContainer.stop()

        // then
        expect {
            that(result).generateJooqClassesTask.outcome isEqualTo SUCCESS
            that(projectFile("build/generated-jooq/org/jooq/generated/tables/Foo.java")).exists()
        }
    }
}
