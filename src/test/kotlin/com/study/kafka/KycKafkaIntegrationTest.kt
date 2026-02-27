package com.study.com.study.kafka

import com.study.config.DatabaseFactory
import com.study.kafka.KycConsumer
import com.study.kafka.KycProducer
import com.study.model.KycRequest
import com.study.model.KycStatus
import com.study.repository.KycRepository
import com.study.service.KycService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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


    @BeforeAll
    fun setup(){
        System.setProperty("DB_URL", postgres.jdbcUrl)
        dsl = DatabaseFactory.init()
        repository = KycRepository(dsl)

        val bootstrapServers = kafka.bootstrapServers
        kycProducer = KycProducer(bootstrapServers)
        service = KycService(repository, kycProducer)
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

    }


}