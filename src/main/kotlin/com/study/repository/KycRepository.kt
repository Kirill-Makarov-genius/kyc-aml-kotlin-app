package com.study.repository

import com.study.generated.tables.references.KYC_REQUESTS
import com.study.model.KycRequest
import com.study.model.KycStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import java.util.UUID

class KycRepository(private val dsl: DSLContext){


    suspend fun save(request: KycRequest): KycRequest = withContext(Dispatchers.IO){
        val record = dsl.newRecord(KYC_REQUESTS)
        record.id = UUID.fromString(request.id)
        record.firstName = request.firstName
        record.lastName = request.lastName
        record.passportNumber = request.passportNumber
        record.status = request.status.name

        // Make INSERT in table KYC_REQUESTS
        record.store()

        request
    }

    suspend fun findById(id: String) : KycRequest? = withContext(Dispatchers.IO){

        val table = KYC_REQUESTS

        val record = dsl.selectFrom(table)
            .where(table.ID.eq(UUID.fromString(id)))
            .fetchOne() ?: return@withContext null

        KycRequest(
            id = record.id.toString(),
            firstName = record.firstName!!,
            lastName = record.lastName!!,
            passportNumber = record.passportNumber!!,
            status = KycStatus.valueOf(record.status!!)
        )
    }



}