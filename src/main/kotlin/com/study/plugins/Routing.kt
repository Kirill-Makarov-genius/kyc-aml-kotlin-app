package com.study.plugins

import com.study.exception.KycValidationException
import com.study.model.KycRequest
import com.study.model.KycRequestWithHistory
import com.study.service.KycAuditService
import com.study.service.KycService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.lang.Exception

fun Application.configureRouting(kycService: KycService, kycAuditService: KycAuditService) {
    routing {
        get("/kyc"){
            call.respond(kycAuditService.getAllRequestsWithLogs())
        }
        post("/kyc"){

            val request = call.receive<KycRequest>()
            val created = kycService.createRequest(request)

            call.respond(HttpStatusCode.Created, created)


        }
        get("/kyc/{id}"){
            val id = call.parameters["id"] ?: throw KycValidationException("Missing ID")

            val request = kycService.getRequest(id)
            call.respond(request)
        }
        post ("/kyc/check/{id}"){
            val id = call.parameters["id"] ?: throw KycValidationException("Missing id")
            val request = kycService.refreshRiskScore(id)
            call.respond(request)
        }
        get("kyc/audit/{id}"){
            val id = call.parameters["id"] ?: throw KycValidationException("Missing ID")

            val request = kycAuditService.getRequestAndLogsById(id) ?: KycRequestWithHistory
            call.respond(request)
        }

        get("/") {
            call.respondText("Hello World!")
        }
    }
}
