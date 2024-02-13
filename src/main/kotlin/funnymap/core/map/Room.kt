package funnymap.core.map

import funnymap.config.Config
import funnymap.core.RoomData
import funnymap.features.dungeon.Dungeon
import funnymap.features.dungeon.DungeonScan
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

    fun getArrayPosition(): Pair<Int, Int> {
        return Pair((x - DungeonScan.startX) / 16, (z - DungeonScan.startZ) / 16)
    }

    fun addToUnique(row: Int, column: Int, roomName: String = data.name) {
        val unique = Dungeon.Info.uniqueRooms.find { it.name == roomName }

        if (unique == null) {
            Dungeon.Info.uniqueRooms.add(UniqueRoom(column, row, this))
        } else {
            unique.addTile(column, row, this)
        }
    }
}
