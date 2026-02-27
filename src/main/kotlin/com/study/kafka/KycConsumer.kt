package com.study.kafka

import com.study.model.KycBatchItem
import com.study.service.KycService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.util.Properties
import java.time.Duration

class KycConsumer(
    bootstrapServers: String,
    private val kycService: KycService,
    private val producer: KycProducer
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val consumer: KafkaConsumer<String, String>


    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "kyc-aml-workers")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            // Batch has maximum 10 messages per time
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10")
        }
        consumer = KafkaConsumer(props)
        consumer.subscribe(listOf("kyc.requests"))
    }

    fun start() = scope.launch {
        log.info("Kafka Consumer started...")
        try{
            while (isActive){
                val records = consumer.poll(Duration.ofMillis(1000))

                if (records.isEmpty) continue

                log.info("Received batch of {} records", records.count())
                val batchRequests = mutableListOf<KycBatchItem>()

                for (record in records){
                    // Get requestId from kafka message, calculate it risk score and add to batch
                    val requestId = record.value()
                    try{
                        val checkedRequest = kycService.calculateAMLRiskScore(requestId)
                        batchRequests.add(checkedRequest)

                    } catch (e: Exception){
                        log.error("FAILED to process record {}. Sending to DLQ. ERROR: {}", record.value(), e.message)

                        producer.sendToDlq(requestId, requestId, e.message ?: "Unknown Error during processing")
                    }
                }

                kycService.updateBatchAmlScoreOfRequests(batchRequests)

                consumer.commitSync()
                log.info("Batch processed and commited")
            }
        } catch (e: CancellationException) {
            log.info("Consumer coroutine is being cancelled")
        } catch (e: Exception){
            log.error("Fatal error in consumer", e)
        } finally {
            withContext(NonCancellable) {
                consumer.close()
                log.info("Kafka consumer connection closed")
            }
        }
    }
    fun stop(){

    }

}