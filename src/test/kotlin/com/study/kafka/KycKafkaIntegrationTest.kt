package com.study.com.study.kafka

import com.study.config.DatabaseFactory
import com.study.kafka.KycConsumer
import com.study.kafka.KycProducer
import com.study.model.KycRequest
import com.study.model.KycStatus
import com.study.repository.KycAuditRepository
import com.study.repository.KycRepository
import com.study.service.KycAuditService
import com.study.service.KycService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertAll
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.UUID
import kotlin.test.assertEquals

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KycKafkaIntegrationTest {


    companion object{

        @Container
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine")

        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
    }

    private lateinit var dsl: DSLContext
    private lateinit var repository: KycRepository
    private lateinit var service: KycService
    private lateinit var kycProducer: KycProducer
    private lateinit var kycConsumer: KycConsumer
    private lateinit var auditRepository: KycAuditRepository
    private lateinit var auditService: KycAuditService

    @BeforeAll
    fun setup(){
        System.setProperty("DB_URL", postgres.jdbcUrl)
        dsl = DatabaseFactory.init()
        repository = KycRepository(dsl)
        auditRepository = KycAuditRepository(dsl)
        val bootstrapServers = kafka.bootstrapServers
        kycProducer = KycProducer(bootstrapServers)
        service = KycService(repository, kycProducer)
        auditService = KycAuditService(auditRepository)
        kycConsumer = KycConsumer(bootstrapServers, service, kycProducer)

        kycConsumer.start()
    }

    @AfterAll
    fun tearDown(){
        kycConsumer.stop()
        kycProducer.close()
    }


    @Test
    fun `should process batch of messages and update database`() = runBlocking {


        // Arrange
        val listOfIds = listOf(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        listOfIds.forEach { id ->
            val newRequest = KycRequest(
                id,
                "USER",
                "TEST",
                "1234 123123"
            )
            service.createRequest(newRequest)
        }
        val badId = UUID.randomUUID().toString()
        service.createRequest(
            KycRequest(
                badId,
                "User",
                "BAD",
                "1234 999999"
            )
        )

        // Assert
        delay(20000)

        listOfIds.forEach { id ->
            val updatedRequest = repository.findById(id)

            requireNotNull(updatedRequest) {"Request can't be null"}
            assertAll(
                {assertEquals(KycStatus.VERIFIED, updatedRequest.status, "Should be verified") },
                {assertEquals(10, updatedRequest.riskScore, "Rish score be 10") }
            )

        }

        val badRequest = repository.findById(badId)
        requireNotNull(badRequest)

        assertAll(
            {assertEquals(KycStatus.BLOCKED, badRequest.status, "Should be blocked")},
            {assertEquals(60, badRequest.riskScore, "Rish score be 10") }
        )




    }


















}