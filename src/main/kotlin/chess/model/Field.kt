package chess.model

import java.util.*

data class Field(val owner: Optional<out Team>, val piece: Optional<out Piece>)