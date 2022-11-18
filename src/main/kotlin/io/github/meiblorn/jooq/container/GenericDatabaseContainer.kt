package io.github.meiblorn.jooq.container

import io.github.meiblorn.jooq.settings.Database
import io.github.meiblorn.jooq.settings.Image
import java.sql.Driver
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.slf4j.LoggerFactory
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.utility.DockerImageName

class GenericDatabaseContainer(
    private val image: Image,
    private val database: Database.Internal,
    private val jdbcAwareClassLoader: ClassLoader,
) : JdbcDatabaseContainer<GenericDatabaseContainer>(DockerImageName.parse(image.name)) {

    private val driverLoadLock = ReentrantLock()
    private var driver: Driver? = null

    init {
        withLogConsumer(
            Slf4jLogConsumer(LoggerFactory.getLogger("JooqGenerationDb[$dockerImageName]")))
        withEnv(image.envVars)
        withExposedPorts(database.port)
        setWaitStrategy(HostPortWaitStrategy())
        image.command.takeUnless { it.isNullOrBlank() }?.run(::withCommand)
    }

    override fun getDriverClassName() = database.jdbc.driverClassName

    override fun getJdbcUrl() = database.getJdbcUrl(host, getMappedPort(database.port))

    override fun getUsername() = database.username

    override fun getPassword() = database.password

    override fun getTestQueryString() = image.testQuery

    override fun getDatabaseName() = database.name

    override fun getJdbcDriverInstance(): Driver {
        if (driver == null) {
            driverLoadLock.withLock {
                if (driver == null) {
                    driver = getNewJdbcDriverInstance()
                }
            }
        }

        return driver!!
    }

    private fun getNewJdbcDriverInstance() =
        try {
            @Suppress("DEPRECATION")
            jdbcAwareClassLoader.loadClass(driverClassName).newInstance() as Driver
        } catch (e: Exception) {
            when (e) {
                is InstantiationException,
                is IllegalAccessException,
                is ClassNotFoundException -> {
                    throw NoDriverFoundException("Could not get Driver", e)
                }
                else -> throw e
            }
        }
}
