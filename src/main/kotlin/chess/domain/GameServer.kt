package chess.domain

import chess.model.Board
import chess.model.Message
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession

interface GameServer {
    suspend fun memberJoin(member: String, socket: WebSocketSession)
    suspend fun memberRenamed(member: String, to: String)
    suspend fun memberLeft(member: String, socket: WebSocketSession)
    suspend fun who(sender: String)
    suspend fun sendTo(recipient: String, sender: String, any: Any)
    suspend fun message(sender: String, any: Any)
    suspend fun broadcast(any: Any)
    suspend fun broadcast(sender: String, any: Any)
    suspend fun broadcast(message: Message)
    suspend fun sendBoard(board: Board)
    suspend fun List<WebSocketSession>.send(frame: Frame)
}