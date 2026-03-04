package com.study.repository

import com.study.generated.tables.records.KycAuditLogRecord
import com.study.generated.tables.references.KYC_AUDIT_LOG
import com.study.generated.tables.references.KYC_REQUESTS
import com.study.mapper.toModel
import com.study.model.KycAuditModel
import com.study.model.KycRequestWithHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import java.util.UUID

class KycAuditRepository(private val dsl: DSLContext) {


    suspend fun findAllByRequestId(requestId: String) : List<KycAuditModel> = withContext(Dispatchers.IO){
        val t = KYC_AUDIT_LOG

        val records = dsl.selectFrom(t)
            .where(t.REQUEST_ID.eq(UUID.fromString(requestId)))
            .orderBy(t.CHANGED_AT.desc())
            .fetch()

        records.map{it.toModel()}

    }

    suspend fun findRequestWithHistory(requestId: String): KycRequestWithHistory? = withContext(Dispatchers.IO){
        val r = KYC_REQUESTS
        val a = KYC_AUDIT_LOG


        val result = dsl.select()
            .from(r)
            .leftJoin(a).on(r.ID.eq(a.REQUEST_ID))
            .where(r.ID.eq(UUID.fromString(requestId)))
            .orderBy(a.CHANGED_AT.desc())
            .fetch()

        if (result.isEmpty()){
            return@withContext null
        }

        val request = result.first().into(r).toModel()

        val history = result
            .filter { it.get(a.ID) != null }
            .map {it.into(a).toModel()}

        KycRequestWithHistory(request, history)

    }

    suspend fun findAllRequestsWithHistory(): List<KycRequestWithHistory> = withContext(Dispatchers.IO){
        val r = KYC_REQUESTS
        val a = KYC_AUDIT_LOG

        val results = dsl.select()
            .from(r)
            .leftJoin(a).on(r.ID.eq(a.REQUEST_ID))
            .fetch()

        val grouped = results.intoGroups(r, a)

        grouped.map{ (requestRecord, auditRecords) ->
            KycRequestWithHistory(
                request = requestRecord.toModel(),
                history = auditRecords
                    .filter { it.id != null }
                    .map { it.toModel() }
            )
        }


    }


}