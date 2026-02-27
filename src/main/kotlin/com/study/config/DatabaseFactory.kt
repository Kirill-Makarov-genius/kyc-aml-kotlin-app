package com.study.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory

object DatabaseFactory {

    private var dataSource: HikariDataSource? = null
    private val log = LoggerFactory.getLogger(javaClass)

    fun init(): DSLContext{
        val config = HikariConfig().apply {

            jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/kyc_db"
            username = "kyc_user"
            password = "kyc_password"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            validate()
        }
        dataSource = HikariDataSource(config)

        log.info("Running Flyway migrations...")
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
        log.info("Flyway migrations finished successfully")

        return DSL.using(dataSource, SQLDialect.POSTGRES)
    }

}