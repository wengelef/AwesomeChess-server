package chess.domain

import chess.model.*
import chess.util.Point
import chess.util.forEachIndexed
import chess.util.toList
import java.util.*
import kotlin.random.Random

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

    fun nextTurn(): Turn {
        ++turnIndex

        val playerIndex = turnIndex.rem(2)

        val player = players[playerIndex]

        val turns = mutableListOf<Turn>()

        board.fields.forEachIndexed { x, y, field ->
            if (field.owner.isPresent && field.owner.get() == player.team) {
                if (field.piece.isPresent) {
                    val piece = field.piece.get()

                    piece.moves.forEach { move ->
                        var newX = x
                        var newY = y

                        move.directions.forEach { (direction, number) ->
                            when (direction) {
                                Direction.Forward -> {
                                    when (player.team) {
                                        Team.White -> newY += number
                                        Team.Black -> newY -= number
                                    }
                                }
                                Direction.Backward -> {
                                    when (player.team) {
                                        Team.White -> newY -= number
                                        Team.Black -> newY += number
                                    }
                                }
                                Direction.Left -> newX -= number
                                Direction.Right -> newX += number
                                //Direction.Diagonally -> TODO()
                            }
                        }

                        if (newX in 0..7 && newY in 0..7) {
                            val field = board.fields[newX, newY]
                            val owner = field.owner
                            if (!owner.isPresent ||
                                    owner.isPresent && owner.get() != player.team) {
                                turns.add(Turn(Point(x, y), Point(newX, newY)))
                            }
                        }
                    }
                }
            }
        }

        val turn = turns[Random.nextInt(turns.size)]

        return turn.apply {
            board.fields[to.x, to.y] = board.fields[from.x, from.y]
            board.fields[from.x, from.y] = Field(Optional.empty(), Optional.empty())

            val numberOfKings = board.fields.toList().count { it.piece.isPresent && it.piece.get() == King }
            isOver = numberOfKings < 2

            if (isOver) {
                winner = Optional.of(player.team)
            }
        }
    }
}