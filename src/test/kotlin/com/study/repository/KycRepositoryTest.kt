package com.study.com.study.repository

import com.study.model.KycRequest
import com.study.model.KycStatus
import com.study.repository.KycRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
class KycRepositoryTest {


    companion object{

        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("kyc_test_db")
            withUsername("test_user")
            withPassword("test_pass")
        }

        lateinit var dataSource: HikariDataSource
        lateinit var dsl: DSLContext
        lateinit var repository: KycRepository

        @JvmStatic
        @BeforeAll
        fun setUp(){
            // Configure Flyway for migrations, set location of migrations, load it and set it in out testcontainer db
            Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .locations("classpath:db/migration")
                .load()
                .migrate()

            // Set connection of postgres to HikariPool
            val config = HikariConfig().apply {
                jdbcUrl = postgres.jdbcUrl
                username = postgres.username
                password = postgres.password
                driverClassName = "org.postgresql.Driver"
                isAutoCommit = true
            }

            dataSource = HikariDataSource(config)
            // Set DSL for jOOQ from HikariDataSource
            dsl = DSL.using(dataSource, SQLDialect.POSTGRES)
            // Initialize repository for testing
            repository = KycRepository(dsl)
        }

        @JvmStatic
        @AfterAll
        fun tearDown(){
            dataSource.close()
        }

    }


    @Test
    fun `should save and find request by id`() = runBlocking {

        // Arrange
        val newRequest = KycRequest(
            firstName = "John",
            lastName = "Doe",
            passportNumber = "1111 2222222"
        )


        // Act
        repository.save(newRequest)
        val foundRequest = repository.findById(newRequest.id)

        requireNotNull(foundRequest) {"Request should be found in DB"}
        assertAll(
            "Checking all fields of kycRequest",
            { assertEquals("John", foundRequest.firstName) },
            { assertEquals(KycStatus.PENDING, foundRequest.status) },
            { assertEquals(0, foundRequest.riskScore) }
        )


    }

    @Test
    fun `should update risk data correctly`() = runBlocking {


        // Arrange
        val request = KycRequest(
            firstName = "Risk",
            lastName = "Guy",
            passportNumber = "1234 56789"
        )
        repository.save(request)

        // Act
        repository.updateRiskData(
            request.id,
            KycStatus.BLOCKED,
            85,
            "Highly suspicious"
        )

        val updated = repository.findById(request.id)
        requireNotNull(updated) {"Request should be found in DB by id"}

        assertAll(
            "Checking all fields of updated kycRequest",
            { assertEquals(KycStatus.BLOCKED, updated.status) },
            {assertEquals(85, updated.riskScore)},
            {assertEquals("Highly suspicious", updated.internalComment)}
        )


    }


}