package com.study.plugins

import com.study.model.KycRequest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult


fun Application.configureValidation(){
    install(RequestValidation){
        validate<KycRequest> { request ->
            val passportRegex = Regex("\\d{4}\\s\\d{6}")

            when {
                request.firstName.isBlank() ->
                    ValidationResult.Invalid("First name cannot be empty")
                request.lastName.isBlank() ->
                    ValidationResult.Invalid("Last name cannot be empty")
                !request.passportNumber.matches(passportRegex) ->
                    ValidationResult.Invalid("Passport must be in format: XXXX XXXXXX (10 digits with space)")
                else -> ValidationResult.Valid
            }
        }
    }
}