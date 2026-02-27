package com.study.kafka

import io.ktor.server.routing.RoutingApplicationRequest
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import java.util.Properties

class KycProducer(bootstrapServers: String){
    private val log = LoggerFactory.getLogger(javaClass)
    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties().apply{
            // Base settings
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)

            // ACKS_CONFIG responsible for confirming the recording.
            // If set '0', broker will just send and forget without checking
            // If set '1', broker will wait confirming from leader
            // If set 'all', broker will wait confirming from leader and all replicas
            put(ProducerConfig.ACKS_CONFIG, "all")
            // Made messages idempotence to prevent duplicates
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
            // Set max retries with network crash
            put(ProducerConfig.RETRIES_CONFIG, Int.MAX_VALUE)
            // But not longer than 2 minutes
            put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000)

            // Batching from Producer
            // Wait 20ms to collect batch of messages
            put(ProducerConfig.LINGER_MS_CONFIG, 20)
            // Max size of batch is 32Kb
            put(ProducerConfig.BATCH_SIZE_CONFIG, 32*1024)
            // Fast compression from Google
            put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip")
        }

        producer = KafkaProducer(props)

    }

    fun sendRequestCreated(requestId: String){
        // Using requestId as a key for splitting message on partitions
        val record = ProducerRecord("kyc.requests", requestId, requestId)

        producer.send(record) { metadata, exception ->
            if (exception != null){
                log.error("Failed to send message to Kafka: {}", exception.message)
            }
            else{
                log.info("Message sent to topic {} partition {} with offset {}",
                    metadata.topic(), metadata.partition(), metadata.offset())
            }
        }
    }
    fun close(){
        producer.close()
    }

}