package funnymap.core

import java.awt.Color

data class Door(override var x: Int, override var z: Int) : Tile(x, z) {

    var type = DoorType.NONE

    override fun toString(): String {
        return type.name
    }

    override fun getColor(): Color {
        return when (this.type) {
            DoorType.WITHER -> Color(0, 0, 0)
            DoorType.BLOOD -> Color(231, 0, 0)
            DoorType.ENTRANCE -> Color(20, 133, 0)
            else -> Color(92, 52, 14)
        }
    }
}
