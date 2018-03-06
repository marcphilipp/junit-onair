package org.junit.onair

import io.ktor.websocket.*
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class TestEventServer {

    private val listeners = ConcurrentHashMap<Session, MutableList<WebSocketSession>>()

    fun register(session: Session, socket: WebSocketSession) {
        val list = listeners.computeIfAbsent(session) { CopyOnWriteArrayList<WebSocketSession>() }
        list.add(socket)
    }

    fun unregister(session: Session, socket: WebSocketSession) {
        listeners[session]?.remove(socket)
    }

    suspend fun sendMessage() {
        listeners.values.forEach { sockets ->
            sockets.forEach { socket ->
                socket.send(Frame.Text("foo at " + Instant.now()))
            }
        }
    }
}
