package funnymap.core

import java.awt.Color

data class Room(override var x: Int, override var z: Int, var data: RoomData) : Tile(x, z) {

    var hasMimic = false
    var isSeparator = false

    override fun toString(): String {
        return data.name
    }

    override fun getColor() = when {
        else -> when (data.type) {
            RoomType.PUZZLE -> Color(117, 0, 133)
            RoomType.BLOOD -> Color(255, 0, 0)
            RoomType.TRAP -> Color(216, 127, 51)
            RoomType.CHAMPION -> Color(254, 223, 0)
            RoomType.FAIRY -> Color(224, 0, 255)
            RoomType.ENTRANCE -> Color(20, 133, 0)
            RoomType.RARE -> Color(255, 203, 89)
            else -> Color(107, 58, 17)
        }
    }
}
