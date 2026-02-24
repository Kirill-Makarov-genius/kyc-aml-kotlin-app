package com.study

import com.study.config.DatabaseFactory
import com.study.plugins.configureRouting
import com.study.plugins.configureSerialization
import com.study.plugins.configureStatusPages
import com.study.repository.KycRepository
import com.study.service.KycService
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val dsl = DatabaseFactory.init()
    val repository = KycRepository(dsl)
    val service = KycService(repository)

    configureStatusPages()

    configureSerialization()

    configureRouting(service)
}
