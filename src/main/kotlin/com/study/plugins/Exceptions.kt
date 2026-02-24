package com.study.plugins

import com.study.exception.KycException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String, val code: String)

fun Application.configureStatusPages(){
    install(StatusPages) {

        exception<KycException> { call, exception ->
            call.respond(
                exception.statusCode,
                ErrorResponse(error = exception.message, code = exception.javaClass.simpleName)
            )
        }

        exception<Throwable> { call, exception ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(error = "Internal server error. Please Try again later.", code = "INTERNAL_ERROR")
            )
        }

    }
}