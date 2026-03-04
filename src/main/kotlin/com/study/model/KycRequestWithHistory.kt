package com.study.model

import kotlinx.serialization.Serializable

@Serializable
data class KycRequestWithHistory(

    val request: KycRequest,
    val history: List<KycAuditModel>


)
