package com.study.mapper

import com.study.generated.tables.KycAuditLog
import com.study.generated.tables.records.KycAuditLogRecord
import com.study.generated.tables.records.KycRequestsRecord
import com.study.model.KycAuditModel
import com.study.model.KycRequest
import com.study.model.KycStatus

fun KycRequestsRecord.toModel(): KycRequest {
    return KycRequest(
        id = this.id?.toString() ?: error("ID is missing"),
        firstName = this.firstName ?: error("First name is missing"),
        lastName = this.lastName ?: error("Last name is missing"),
        passportNumber = this.passportNumber ?: error("Passport is missing"),
        status = KycStatus.valueOf(this.status ?: error("Passport is missing")),
        riskScore = this.riskScore ?: 0,
        internalComment = this.internalComment
    )
}

fun KycAuditLogRecord.toModel(): KycAuditModel = KycAuditModel(
    id = this.id?.toString() ?: error("Log ID is missing"),
    requestId = this.requestId?.toString() ?: error("Request ID is missing"),
    oldStatus = KycStatus.valueOf(this.oldStatus!!),
    newStatus = KycStatus.valueOf(this.newStatus!!),
    reason = this.reason,
    changedAt = this.changedAt?.toString() ?: ""
)