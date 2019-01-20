package chess.model

import chess.util.Point

class Turn(val from: Point, val to: Point) {
  override fun toString(): String = "Turn ($from to $to)"
}