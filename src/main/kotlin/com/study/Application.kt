package com.study

import org.slf4j.event.Level
import com.study.config.DatabaseFactory
import com.study.plugins.configureMonitoring
import com.study.plugins.configureRouting
import com.study.plugins.configureSerialization
import com.study.plugins.configureStatusPages
import com.study.plugins.configureValidation
import com.study.repository.KycRepository
import com.study.service.KycService
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(CallLogging){
        level = Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            "HTTP $httpMethod $path -> $status"
        }
    }

    val dsl = DatabaseFactory.init()
    val repository = KycRepository(dsl)
    val service = KycService(repository)

    configureMonitoring()
    configureValidation()
    configureStatusPages()
    configureSerialization()
    configureRouting(service)
}
