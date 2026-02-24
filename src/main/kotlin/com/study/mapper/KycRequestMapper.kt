package com.study.mapper

import com.study.generated.tables.records.KycRequestsRecord
import com.study.model.KycRequest
import com.study.model.KycStatus

fun KycRequestsRecord.toModel(): KycRequest {
    return KycRequest(
        id = this.id?.toString() ?: error("ID is missing"),
        firstName = this.firstName ?: error("First name is missing"),
        lastName = this.lastName ?: error("Last name is missing"),
        passportNumber = this.passportNumber ?: error("Passport is missing"),
        status = KycStatus.valueOf(this.status ?: KycStatus.PENDING.name),
        riskScore = this.riskScore ?: 0,
        internalComment = this.internalComment
    )
}