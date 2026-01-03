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

        val flywayLocations = conf.getString("flyway.locations")
//        val flyway = Flyway.configure()
//            .dataSource(dataSource)
//            .locations(flywayLocations)
//            .baselineOnMigrate(true)
//            .load()

        val cl = Thread.currentThread().contextClassLoader
        val res = cl.getResource("db/migration/R__init_core.sql")
        log.info("Migration resource visible? ${res != null} -> $res")

//        val flyway = Flyway.configure()
//            .dataSource(dataSource)
//            .locations(flywayLocations)
//            .sqlMigrationPrefix("V")
//            .sqlMigrationSeparator("__")
//            .sqlMigrationSuffixes(".sql")
//            .baselineOnMigrate(true)
//            .load()
//
//        val result = flyway.migrate()

        //temp
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations(flywayLocations)
            .baselineOnMigrate(true)
            .load()

        val result = flyway.migrate()
        log.info("Flyway migration result: {}", result)

        val db = Database.connect(dataSource)
        log.info("Connected to Postgres")
        return db
    }

    private fun resolveDb(conf: Config): DbResolved {
        // Render-style single URL
        val databaseUrl = System.getenv("DATABASE_URL")?.takeIf { it.isNotBlank() }
        if (databaseUrl != null) {
            val uri = URI(databaseUrl)
            val host = uri.host ?: error("DATABASE_URL missing host")
            val port = if (uri.port == -1) 5432 else uri.port
            val dbName = uri.path.removePrefix("/")

            val userInfo = uri.userInfo ?: error("DATABASE_URL missing user:pass")
            val (user, pass) = userInfo.split(":", limit = 2).let { it[0] to (it.getOrNull(1) ?: "") }

            val jdbc = "jdbc:postgresql://$host:$port/$dbName"

            val maxPool = conf.getIntOrDefault("db.maximumPoolSize", 10)
            return DbResolved(jdbc, user, pass, maxPool)
        }

        // Existing DB_* path (from application.conf)
        if (conf.hasPath("db.jdbcUrl") && conf.hasPath("db.username") && conf.hasPath("db.password")) {
            val dbConf = conf.getConfig("db")
            val jdbcUrl = dbConf.getString("jdbcUrl")
            val username = dbConf.getString("username")
            val password = dbConf.getString("password")
            val maxPool = if (dbConf.hasPath("maximumPoolSize")) dbConf.getInt("maximumPoolSize") else 10
            return DbResolved(jdbcUrl, username, password, maxPool)
        }

        // Local defaults (matches your docker-compose.yml)
        val jdbc = "jdbc:postgresql://localhost:5432/quickpay"
        return DbResolved(jdbc, "quickpay", "quickpay", 10)
    }

    private data class DbResolved(
        val jdbcUrl: String,
        val username: String,
        val password: String,
        val maxPool: Int
    )

    private fun Config.getIntOrDefault(path: String, defaultValue: Int): Int =
        if (this.hasPath(path)) this.getInt(path) else defaultValue
}