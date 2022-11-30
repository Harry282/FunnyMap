package funnymap.core.map

import java.awt.Color

abstract class Tile(val x: Int, val z: Int) {
    var state = RoomState.UNDISCOVERED
    abstract val color: Color
}
