package com.study.model

data class KycBatchItem(
    val id: String,
    val oldStatus: KycStatus,
    val newStatus: KycStatus,
    val riskScore: Int,
    val comment: String
)
