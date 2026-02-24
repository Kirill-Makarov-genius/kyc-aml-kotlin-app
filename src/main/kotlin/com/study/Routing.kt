package com.study

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
            try{
                val request = call.receive<KycRequest>()
                val created = service.createRequest(request)

                call.respond(HttpStatusCode.Created, created)
            }
            catch (e: Exception){
                call.respond(HttpStatusCode.BadRequest, "Invalid data: ${e.message}")
            }
        }
        get("/kyc/{id}"){
            val id = call.parameters["id"]
            if (id == null){
                call.respond(HttpStatusCode.BadRequest, "Missing ID")
            }
            else{
                val request = service.getRequest(id)
                if (request != null){
                    call.respond(request)
                }
                else{
                    call.respond(HttpStatusCode.NotFound, "Request not found")
                }
            }
        }

        get("/") {
            call.respondText("Hello World!")
        }
    }
}
