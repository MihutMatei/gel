package kronos.project.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import kronos.project.models.CommentsTable
import kronos.project.models.PinImagesTable
import kronos.project.models.PinsTable
import kronos.project.models.UserSettingsTable
import kronos.project.models.UsersTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val initOnStartup = config.propertyOrNull("database.initOnStartup")?.getString()?.toBooleanStrictOrNull() ?: true
        if (!initOnStartup) return

        val hikariConfig = HikariConfig().apply {
            val resolvedUrl = System.getenv("JDBC_DATABASE_URL")
                ?: config.propertyOrNull("database.url")?.getString()
                ?: error("Missing database URL. Set JDBC_DATABASE_URL or database.url")
            jdbcUrl = resolvedUrl
            username = System.getenv("DB_USER")
                ?: config.propertyOrNull("database.user")?.getString()
                ?: error("Missing database user. Set DB_USER or database.user")
            password = System.getenv("DB_PASSWORD")
                ?: config.propertyOrNull("database.password")?.getString()
                ?: error("Missing database password. Set DB_PASSWORD or database.password")
            driverClassName = config.propertyOrNull("database.driver")?.getString()
                ?: when {
                    resolvedUrl.startsWith("jdbc:postgresql:") -> "org.postgresql.Driver"
                    resolvedUrl.startsWith("jdbc:h2:") -> "org.h2.Driver"
                    else -> error("Unsupported JDBC URL. Provide database.driver explicitly.")
                }
            maximumPoolSize = config.propertyOrNull("database.maxPoolSize")?.getString()?.toIntOrNull() ?: 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(hikariConfig))

        transaction {
            // Keep existing data; only add missing tables/columns for evolving schemas.
            SchemaUtils.createMissingTablesAndColumns(UsersTable, UserSettingsTable, PinsTable, CommentsTable, PinImagesTable)
        }
    }

    fun <T> dbQuery(block: () -> T): T = transaction { block() }
}

