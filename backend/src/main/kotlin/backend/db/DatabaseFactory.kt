package backend.db

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val log = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init(): Database {
        val conf = ConfigFactory.load()
        val dbConf = conf.getConfig("db")
        val jdbcUrl = dbConf.getString("jdbcUrl")
        val username = dbConf.getString("username")
        val password = dbConf.getString("password")
        val maxPool = if (dbConf.hasPath("maximumPoolSize")) dbConf.getInt("maximumPoolSize") else 10

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

        // Flyway
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations(ConfigFactory.load().getString("flyway.locations"))
            .baselineOnMigrate(true)
            .load()

        val result = flyway.migrate()
        log.info("Flyway migration result: $result")

        val db = Database.connect(dataSource)
        log.info("Connected to Postgres")
        return db
    }
}