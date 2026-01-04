package backend.db

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.net.URI

object DatabaseFactory {
    private val log = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init(): Database {
        val conf = ConfigFactory.load()

        val (jdbcUrl, username, password, maxPool) = resolveDb(conf)

        val hikari = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = maxPool
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validate()
        }

        val dataSource = HikariDataSource(hikari)

        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .validateMigrationNaming(true)
            .baselineOnMigrate(true)
            .load()

        val result = flyway.migrate()
        log.info(
            "Flyway applied: {}, success: {}",
            result.migrationsExecuted,
            result.success
        )

        val db = Database.connect(dataSource)
        log.info("Connected to Postgres")
        return db
    }

    private fun resolveDb(conf: Config): DbResolved {
        // Render-style DATABASE_URL
        val databaseUrl = System.getenv("DATABASE_URL")?.takeIf { it.isNotBlank() }
        if (databaseUrl != null) {
            val uri = URI(databaseUrl)
            val host = uri.host ?: error("DATABASE_URL missing host")
            val port = if (uri.port == -1) 5432 else uri.port
            val dbName = uri.path.removePrefix("/")

            val userInfo = uri.userInfo ?: error("DATABASE_URL missing user:pass")
            val (user, pass) = userInfo.split(":", limit = 2)
                .let { it[0] to (it.getOrNull(1) ?: "") }

            val jdbc = "jdbc:postgresql://$host:$port/$dbName?sslmode=require"
            val maxPool = conf.getIntOrDefault("db.maximumPoolSize", 10)

            return DbResolved(jdbc, user, pass, maxPool)
        }

        // application.conf DB config
        if (conf.hasPath("db.jdbcUrl")) {
            val dbConf = conf.getConfig("db")
            val jdbcUrl = dbConf.getString("jdbcUrl")
            val username = dbConf.getString("username")
            val password = dbConf.getString("password")
            val maxPool = dbConf.getIntOrDefault("maximumPoolSize", 10)

            return DbResolved(jdbcUrl, username, password, maxPool)
        }

        // Local fallback
        return DbResolved(
            "jdbc:postgresql://localhost:5432/quickpay",
            "quickpay",
            "quickpay",
            10
        )
    }

    private data class DbResolved(
        val jdbcUrl: String,
        val username: String,
        val password: String,
        val maxPool: Int
    )

    private fun Config.getIntOrDefault(path: String, defaultValue: Int): Int =
        if (hasPath(path)) getInt(path) else defaultValue
}