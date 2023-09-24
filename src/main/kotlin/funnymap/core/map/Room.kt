package funnymap.core.map

import funnymap.FunnyMap.Companion.config
import funnymap.core.RoomData
import funnymap.features.dungeon.MimicDetector
import funnymap.features.dungeon.ScoreCalc
import java.awt.Color
import java.awt.Point

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    var direction: Direction? = null
    var corner: Point? = null
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    var secretsfound: Int = 0
    override val color: Color
        get() = when (data.type) {
            RoomType.BLOOD -> config.colorBlood
            RoomType.CHAMPION -> config.colorMiniboss
            RoomType.ENTRANCE -> config.colorEntrance
            RoomType.FAIRY -> config.colorFairy
            RoomType.PUZZLE -> config.colorPuzzle
            RoomType.RARE -> config.colorRare
            RoomType.TRAP -> config.colorTrap
            else -> if (MimicDetector.mimicRoom == data.name &&! ScoreCalc.mimicKilled) config.colorRoomMimic else config.colorRoom
        }
}
