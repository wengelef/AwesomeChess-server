package chess.model

enum class PieceType { King, Queen, Rook, Bishop, Knight, Pawn, None }

sealed class Piece(val type: PieceType) {
    override fun toString(): String {
        return javaClass.simpleName
    }

        object None : Piece(PieceType.None)
}


object King : Piece(PieceType.King)

object Queen : Piece(PieceType.Queen)

object Rook : Piece(PieceType.Rook)

object Bishop : Piece(PieceType.Bishop)

object Knight : Piece(PieceType.Knight)

object Pawn : Piece(PieceType.Pawn)