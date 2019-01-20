package chess.model

import chess.util.Point
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

data class Board(var fields: Array<Array<Field>> = getDefaultBoard()) {

  init {
    reset()
  }

  fun reset() {
    fields = getDefaultBoard()
  }

  fun asSerializable(): Array<Array<NetField>> {
    val returnValue = arrayOfNulls<Array<NetField>>(8)
    for (i in 0..7) {
      val column = arrayOf(
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None),
              NetField(Team.None, PieceType.None))

      for (j in 0..7) {
        val field = fields[j][i]
        if (field.owner.isPresent) {
          column[j] = NetField(field.owner.get(), field.piece.get().type)
        }
      }
      returnValue[i] = column
    }
    return returnValue as Array<Array<NetField>>
  }

  fun getKings(): Int {
    return fields.sumBy { column ->
      column.count { field -> field.owner.isPresent && field.piece.get().type == PieceType.King }
    }
  }

  suspend fun turnsForField(col: Int, row: Int): List<Turn> = coroutineScope {
    val self = fields[col][row]
    val piece = self.piece.get()

    when (piece) {
      is Pawn -> turnsForPawn(col, row)
      King -> turnsForKing(col, row)
      Queen -> turnsForQueen(col, row)
      Rook -> turnsForRook(col, row)
      Bishop -> turnsForBishop(col, row)
      Knight -> turnsForKnight(col, row)
      Piece.None -> emptyList()
    }
  }

  private fun hitTest(from: Field, x: Int, y: Int): HitTestResult {
    return if (x in 0..7 && y in 0..7) {
      val to = fields[x][y]
      val owner = to.owner

      if (owner.isPresent && owner.get() == from.owner.get()) {
        Self
      } else if (!owner.isPresent ||
              owner.isPresent && owner.get() != from.owner.get()) {
        Enemy(x, y)
      } else {
        None(x, y)
      }
    } else {
      OutOfBounds
    }
  }

  private fun turnsForKnight(col: Int, row: Int): List<Turn> {
    val turns = mutableListOf<Turn>()
    val self = fields[col][row]

    when (val result = hitTest(self, col + 1, row + 2)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    when (val result = hitTest(self, col + 2, row + 1)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    when (val result = hitTest(self, col - 1, row - 2)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    when (val result = hitTest(self, col -2, row - 1)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    return turns
  }

  private fun turnsForBishop(col: Int, row: Int): List<Turn> {
    val turns = mutableListOf<Turn>()
    val self = fields[col][row]

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col - i, row - i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    loop@for (i in 1..7) {
      when (val result = hitTest(self, col - i, row + i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    loop@for (i in 1..7) {
      when (val result = hitTest(self, col + i, row - i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    loop@for (i in 1..7) {
      when (val result = hitTest(self, col + i, row + i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    return turns
  }

  private fun turnsForRook(col: Int, row: Int): List<Turn> {
    val turns = mutableListOf<Turn>()
    val self = fields[col][row]

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col, row + i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col, row - i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col + i, row)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col - i, row)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    return turns
  }


  private fun turnsForPawn(col: Int, row: Int): List<Turn> {
    val turns = mutableListOf<Turn>()
    val self = fields[col][row]

    when (self.owner.get()) {
      Team.White -> {
        when (val result = hitTest(self, col, row + 1)) {
          is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        }
      }
      Team.Black -> {
        when (val result = hitTest(self, col, row - 1)) {
          is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        }
      }
    }
    return turns
  }

  private fun turnsForQueen(col: Int, row: Int): List<Turn> {
    val turns = mutableListOf<Turn>()
    val self = fields[col][row]

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col, row + i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col, row - i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col + i, row)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col - i, row)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }

    loop@for (i in 1..7) {
      when (val result = hitTest(self, col - i, row - i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    loop@for (i in 1..7) {
      when (val result = hitTest(self, col - i, row + i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    loop@for (i in 1..7) {
      when (val result = hitTest(self, col + i, row - i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    loop@for (i in 1..7) {
      when (val result = hitTest(self, col + i, row + i)) {
        is Enemy -> {
          turns.add(Turn(Point(col, row), Point(result.x, result.y)))
          break@loop
        }
        is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
        is Self, is OutOfBounds -> break@loop
      }
    }
    return turns
  }

  private fun turnsForKing(col: Int, row: Int): List<Turn> {
    val turns = mutableListOf<Turn>()
    val self = fields[col][row]

    when (val result = hitTest(self, col + 1, row + 1)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    when (val result = hitTest(self, col - 1, row - 1)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    when (val result = hitTest(self, col - 1, row + 1)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }
    when (val result = hitTest(self, col, row - 1)) {
      is Enemy -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
      is None -> turns.add(Turn(Point(col, row), Point(result.x, result.y)))
    }

    return turns
  }
}

sealed class HitTestResult
data class Enemy(val x: Int, val y: Int) : HitTestResult()
data class None(val x: Int, val y: Int) : HitTestResult()
object OutOfBounds : HitTestResult()
object Self : HitTestResult()

data class NetField(val team: Team, val piece: PieceType)

private fun getDefaultBoard(): Array<Array<Field>> {
  val fields = arrayOfNulls<Array<Field>>(8)
  for (col in 0..7) {
    val column = arrayOfNulls<Field>(8)
    for (row in 0..7) {
      val team = when (row) {
        0, 1 -> Optional.of(Team.White)
        6, 7 -> Optional.of(Team.Black)
        else -> Optional.empty()
      }

      val piece = if (row == 0 || row == 7) {
        when (col) {
          0, 7 -> Optional.of(Rook)
          1, 6 -> Optional.of(Knight)
          2, 5 -> Optional.of(Bishop)
          3 -> if (team == Optional.of(Team.White)) {
            Optional.of(King)
          } else {
            Optional.of(Queen)
          }
          4 -> if (team == Optional.of(Team.White)) {
            Optional.of(Queen)
          } else {
            Optional.of(King)
          }
          else -> throw IllegalArgumentException("Field out of Bounds at $col, $row")
        }
      } else if (row == 1 || row == 6) {
        Optional.of(Pawn)
      } else {
        Optional.empty()
      }

      column[row] = Field(team, piece)
    }
    fields[col] = column as Array<Field>
  }
  return fields as Array<Array<Field>>
}