package com.study.model

import io.ktor.websocket.CloseReason
import kotlinx.serialization.Serializable

@Serializable
data class KycAuditModel(
    val id: String,
    val requestId: String,
    val oldStatus: KycStatus,
    val newStatus: KycStatus,
    val reason: String?,
    val changedAt: String
)
