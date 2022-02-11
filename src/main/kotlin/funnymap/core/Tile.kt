package funnymap.core

import java.awt.Color

abstract class Tile(open var x: Int, open var z: Int) {

    var state = RoomState.UNDISCOVERED

    override fun toString(): String {
        return "?"
    }

    abstract fun getColor(): Color
}
