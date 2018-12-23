package chess.model

data class Message(val sender: String? = null, val action: String = "message", val message: Any)