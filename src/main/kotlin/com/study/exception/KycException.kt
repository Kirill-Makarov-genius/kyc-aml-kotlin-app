package com.study.exception

import io.ktor.http.HttpStatusCode

sealed class KycException(
    override val message: String,
    val statusCode: HttpStatusCode
) : RuntimeException(message)

class KycRequestNotFoundException(id: String) :
        KycException("KYC request with ID $id not found", HttpStatusCode.NotFound)

class KycValidationException(message: String) :
        KycException(message, HttpStatusCode.BadRequest)

class KycConflictException(message: String) :
        KycException(message, HttpStatusCode.Conflict)