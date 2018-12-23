package chess.domain

import chess.model.Board
import chess.model.Message
import com.google.gson.Gson
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.close
import kotlinx.coroutines.channels.ClosedSendChannelException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class GameServerImpl(private val gson: Gson) : GameServer {

    val usersCounter = AtomicInteger()
    val memberNames = ConcurrentHashMap<String, String>()
    val members = ConcurrentHashMap<String, MutableList<WebSocketSession>>()
    val lastMessages = LinkedList<Message>()

    override suspend fun memberJoin(member: String, socket: WebSocketSession) {
        val name = memberNames.computeIfAbsent(member) { "user${usersCounter.incrementAndGet()}"}

        val list = members.computeIfAbsent(member) { CopyOnWriteArrayList() }
        list.add(socket)

        if (list.size == 1) {
            broadcast("server", "Member joined: $name")
        }

        val messages = synchronized(lastMessages) { lastMessages.toList() }
        for (message in messages) {
            socket.send(Frame.Text(gson.toJson(message)))
        }
    }

    override suspend fun memberRenamed(member: String, to: String) {
        val oldName = memberNames.put(member, to) ?: member
        broadcast("server", "Member renamed from $oldName to $to")
    }

    override suspend fun memberLeft(member: String, socket: WebSocketSession) {
        val connections = members[member]
        connections?.remove(socket)

        if (connections != null && connections.isEmpty()) {
            val name = memberNames.remove(member) ?: member
            broadcast("server", "Member left : $name")
        }
    }

    override suspend fun who(sender: String) {
        members[sender]?.send(Frame.Text(memberNames.values.joinToString(prefix = "[server::who] ")))
    }

    override suspend fun sendTo(recipient: String, sender: String, any: Any) {
        members[recipient]?.send(Frame.Text(gson.toJson(Message(sender, message = any))))
    }

    override suspend fun message(sender: String, any: Any) {
        val name = memberNames[sender] ?: sender
        val message = Message(name, message = any)

        broadcast(message)

        synchronized(lastMessages) {
            lastMessages.add(message)
            if (lastMessages.size > 100) {
                lastMessages.removeFirst()
            }
        }
    }

    override suspend fun broadcast(any: Any) {
        broadcast(Message(message = any))
    }

    override suspend fun broadcast(message: Message) {
        members.values.forEach { socket -> socket.send(Frame.Text(gson.toJson(message))) }
    }

    override suspend fun sendBoard(board: Board) {
        broadcast(Message("server", "board", board.asSerializable()))
    }

    override suspend fun broadcast(sender: String, any: Any) {
        val name = memberNames[sender] ?: sender
        broadcast(Message(name, message = any))
    }

    override suspend fun List<WebSocketSession>.send(frame: Frame) {
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {

                }
            }
        }
    }
}

