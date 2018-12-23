package chess.model


enum class Direction { Forward, Backward, Left, Right, Diagonally }

enum class PieceType { King, Queen, Rook, Bishop, Knight, Pawn, None }

data class Move(val directions: List<Pair<Direction, Int>>)

sealed class Piece(val type: PieceType, val moves: List<Move>) {
    override fun toString(): String {
        return javaClass.simpleName
    }
}


object King : Piece(PieceType.King, listOf(
        Move(listOf(
                Direction.Forward to 1
        )),
        Move(listOf(
                Direction.Backward to 1
        )),
        Move(listOf(
                Direction.Left to 1
        )),
        Move(listOf(
                Direction.Right to 1
        )),
        Move(listOf(
                Direction.Diagonally to 1
        ))
))

object Queen : Piece(PieceType.Queen, listOf(
        Move(listOf(
                Direction.Forward to 0
        )),
        Move(listOf(
                Direction.Backward to 0
        )),
        Move(listOf(
                Direction.Left to 0
        )),
        Move(listOf(
                Direction.Right to 0
        )),
        Move(listOf(
                Direction.Diagonally to 0
        ))
))

object Rook : Piece(PieceType.Rook, listOf(
        Move(listOf(
                Direction.Forward to 0
        )),
        Move(listOf(
                Direction.Backward to 0
        )),
        Move(listOf(
                Direction.Left to 0
        )),
        Move(listOf(
                Direction.Right to 0
        ))
))

object Bishop : Piece(PieceType.Bishop, listOf(
        Move(listOf(
                Direction.Diagonally to 0
        ))
))

object Knight : Piece(PieceType.Knight, listOf(
        Move(listOf(
                Direction.Forward to 2,
                Direction.Left to 1
        )),
        Move(listOf(
                Direction.Forward to 2,
                Direction.Right to 1
        )),
        Move(listOf(
                Direction.Backward to 2,
                Direction.Left to 1
        )),
        Move(listOf(
                Direction.Backward to 2,
                Direction.Right to 1
        )),
        Move(listOf(
                Direction.Left to 2,
                Direction.Forward to 1
        )),
        Move(listOf(
                Direction.Left to 2,
                Direction.Backward to 1
        )),
        Move(listOf(
                Direction.Right to 2,
                Direction.Forward to 1
        )),
        Move(listOf(
                Direction.Right to 2,
                Direction.Backward to 1
        ))
))

object Pawn : Piece(PieceType.Pawn, listOf(Move(listOf(Direction.Forward to 1))))

object None : Piece(PieceType.None, emptyList())