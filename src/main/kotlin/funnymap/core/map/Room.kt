package funnymap.core.map

import funnymap.config.Config
import funnymap.core.RoomData
import java.awt.Color

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var hasMimic = false
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = if (Config.legitTest && state == RoomState.UNOPENED) Config.colorUnopened
        else when (data.type) {
            RoomType.BLOOD -> Config.colorBlood
            RoomType.CHAMPION -> Config.colorMiniboss
            RoomType.ENTRANCE -> Config.colorEntrance
            RoomType.FAIRY -> Config.colorFairy
            RoomType.PUZZLE -> Config.colorPuzzle
            RoomType.RARE -> Config.colorRare
            RoomType.TRAP -> Config.colorTrap
            else -> if (hasMimic) Config.colorRoomMimic else Config.colorRoom
        }
}
