package org.junit.onair

import io.ktor.application.*
import io.ktor.content.resource
import io.ktor.features.CallLogging
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.util.nextNonce
import io.ktor.websocket.*
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.lang.Integer.parseInt
import java.time.Duration

private val testEventServer = TestEventServer()

fun main(args: Array<String>) {
    System.setProperty("io.netty.noUnsafe", "true")
    val port = parseInt(System.getProperty("org.junit.onair.port", "8080"))
    val server = embeddedServer(Netty, port = port) {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(5)
        }
        install(CallLogging)
        routing {
            resource("/", "index.html")

            install(Sessions) {
                cookie<Session>("SESSION")
            }

            intercept(ApplicationCallPipeline.Infrastructure) {
                if (call.sessions.get<Session>() == null) {
                    call.sessions.set(Session(nextNonce()))
                }
            }

            webSocket("/events") {
                val session = call.sessions.get<Session>()
                if (session == null) {
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                    return@webSocket
                }
                testEventServer.register(session, this)
                try {
                    incoming.receive()
                } finally {
                    testEventServer.unregister(session, this)
                }
            }
        }
    }
    launch {
        while (true) {
            delay(1000)
            testEventServer.sendMessage()
        }
    }
    server.start(wait = true)
}

