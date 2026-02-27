package com.study.repository

import com.study.generated.tables.references.KYC_AUDIT_LOG
import com.study.generated.tables.references.KYC_REQUESTS
import com.study.mapper.toModel
import com.study.model.KycBatchItem
import com.study.model.KycRequest
import com.study.model.KycStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.util.UUID

class KycRepository(private val dsl: DSLContext){


    suspend fun save(request: KycRequest): KycRequest = withContext(Dispatchers.IO){
        val record = dsl.newRecord(KYC_REQUESTS)
        record.id = UUID.fromString(request.id)
        record.firstName = request.firstName
        record.lastName = request.lastName
        record.passportNumber = request.passportNumber
        record.status = request.status.name

        // ВАЖНО: Если в БД колонки NOT NULL, их нужно заполнить тут!
        record.riskScore = 0
        record.internalComment = "Initial request"
        // Make INSERT in table KYC_REQUESTS
        record.store()

        request
    }

    suspend fun findById(id: String) : KycRequest? = withContext(Dispatchers.IO){

        val table = KYC_REQUESTS

        dsl.selectFrom(table)
            .where(table.ID.eq(UUID.fromString(id)))
            .fetchOne()
            ?.toModel()


    }

    suspend fun findAll() : List<KycRequest> = withContext(Dispatchers.IO){

        val table = KYC_REQUESTS

        dsl.selectFrom(table)
            .fetchInto(KycRequest::class.java)

    }

    suspend fun updateRiskData(id: String, oldStatus: KycStatus, newStatus: KycStatus, riskScore: Int, comment: String) = withContext(Dispatchers.IO){

        dsl.transaction { configuration ->
            val tx = DSL.using(configuration)
            val requestId = UUID.fromString(id)


            tx.update(KYC_REQUESTS)
                .set(KYC_REQUESTS.RISK_SCORE, riskScore)
                .set(KYC_REQUESTS.STATUS, newStatus.name)
                .set(KYC_REQUESTS.INTERNAL_COMMENT, comment)
                .where(KYC_REQUESTS.ID.eq(requestId))
                .execute()

            tx.insertInto(KYC_AUDIT_LOG)
                .set(KYC_AUDIT_LOG.ID, UUID.randomUUID())
                .set(KYC_AUDIT_LOG.REQUEST_ID, requestId)
                .set(KYC_AUDIT_LOG.OLD_STATUS, oldStatus.name)
                .set(KYC_AUDIT_LOG.NEW_STATUS, newStatus.name)
                .set(KYC_AUDIT_LOG.REASON, comment)
                .execute()

        }

    }

    suspend fun batchUpdateRiskData(items: List<KycBatchItem>) = withContext(Dispatchers.IO){
        if (items.isEmpty()) return@withContext

        dsl.transaction { configuration ->
            val tx = DSL.using(configuration)

            // Preparing list with updating kyc requests
            val updatedQueries = items.map { item ->
                tx.update(KYC_REQUESTS)
                    .set(KYC_REQUESTS.RISK_SCORE, item.riskScore)
                    .set(KYC_REQUESTS.STATUS, item.newStatus.name)
                    .set(KYC_REQUESTS.INTERNAL_COMMENT, item.comment)
                    .where(KYC_REQUESTS.ID.eq(UUID.fromString(item.id)))
            }
            // Preparing list with inserts queries for kyc audit log
            val insertQueries = items.map { item ->
                tx.insertInto(KYC_AUDIT_LOG)
                    .set(KYC_AUDIT_LOG.ID, UUID.randomUUID())
                    .set(KYC_AUDIT_LOG.REQUEST_ID, UUID.fromString(item.id))
                    .set(KYC_AUDIT_LOG.OLD_STATUS, item.oldStatus.name)
                    .set(KYC_AUDIT_LOG.NEW_STATUS, item.newStatus.name)
                    .set(KYC_AUDIT_LOG.REASON, item.comment)
            }

            // Execute queries as 2 two batches, first for update, second for insert
            tx.batch(updatedQueries).execute()
            tx.batch(insertQueries).execute()

        }
    }



}