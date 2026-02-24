package com.study.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class KycRequest(

    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val passportNumber: String,
    val status: KycStatus = KycStatus.PENDING,
    val riscScore: Int = 0,
    val internalComment: String? = null


)
enum class KycStatus{
    PENDING, VERIFIED, BLOCKED
}
