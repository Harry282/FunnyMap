package funnymap.core.map

import funnymap.FunnyMap.Companion.config
import funnymap.core.RoomData
import java.awt.Color

class Room(x: Int, z: Int, var data: RoomData) : Tile(x, z) {

    var core = 0
    var hasMimic = false
    var isSeparator = false

    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> config.colorBlood
            RoomType.CHAMPION -> config.colorMiniboss
            RoomType.ENTRANCE -> config.colorEntrance
            RoomType.FAIRY -> config.colorFairy
            RoomType.PUZZLE -> config.colorPuzzle
            RoomType.RARE -> config.colorRare
            RoomType.TRAP -> config.colorTrap
            else -> if (hasMimic) config.colorRoomMimic else config.colorRoom
        }
}
