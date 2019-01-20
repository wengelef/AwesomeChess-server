package chess.domain

import chess.model.*
import java.util.*
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class Game(
        private val players: ArrayList<Player>,
        val board: Board) {

  var isOver: Boolean = false

  var winner: Optional<Team> = Optional.empty()

  private var turnIndex = -1

  fun start() {
    isOver = false
    board.reset()
    turnIndex = -1
  }

  suspend fun nextTurn(): Turn {
    ++turnIndex

    val playerIndex = turnIndex.rem(2)

    val player = players[playerIndex]

    val turns = mutableListOf<Turn>()

    for (col in 0..7) {
      for (row in 0..7) {
        val field = board.fields[col][row]

        if (field.owner.isPresent && field.owner.get() == player.team) {
          turns.addAll(board.turnsForField(col, row))
        }
      }
    }

    val turn = turns[Random.nextInt(turns.size)]

    return turn.apply {
      board.fields[to.x][to.y] = board.fields[from.x][from.y]
      board.fields[from.x][from.y] = Field(Optional.empty(), Optional.empty())

      isOver = board.getKings() < 2

      if (isOver) {
        winner = Optional.of(player.team)
      }
    }
  }
}