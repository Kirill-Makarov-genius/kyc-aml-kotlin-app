package com.study

import com.study.config.DatabaseFactory
import com.study.model.KycRequest
import com.study.repository.KycRepository
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.EngineMain
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    val dsl = DatabaseFactory.init()
    val repository = KycRepository(dsl)

    runBlocking {
        println("Saving new requests...")
        val newRequest = KycRequest(
            firstName = "Test",
            lastName = "User",
            passportNumber = "1234 567890"
        )
        repository.save(newRequest)
        println("Saved: $newRequest")

        println("Reading from DB...")
        val fromDb = repository.findById(newRequest.id)
        println("Found: $fromDb")
    }

    configureSerialization()
    configureRouting()
}
