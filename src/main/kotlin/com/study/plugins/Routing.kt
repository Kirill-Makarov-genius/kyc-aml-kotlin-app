package com.study.plugins

import com.study.exception.KycValidationException
import com.study.model.KycRequest
import com.study.service.KycService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.lang.Exception

fun Application.configureRouting(service: KycService) {
    routing {
        get("/kyc"){
            call.respond(service.getAllRequests())
        }
        post("/kyc"){

            val request = call.receive<KycRequest>()
            val created = service.createRequest(request)

            call.respond(HttpStatusCode.Created, created)


        }
        get("/kyc/{id}"){
            val id = call.parameters["id"] ?: throw KycValidationException("Missing ID")

            val request = service.getRequest(id)
            call.respond(request)
        }

        get("/") {
            call.respondText("Hello World!")
        }
    }
}
