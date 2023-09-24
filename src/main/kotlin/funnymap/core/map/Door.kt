package funnymap.core.map

import funnymap.FunnyMap.Companion.config
import java.awt.Color

class Door(override val x: Int, override val z: Int) : Tile {
    var type = DoorType.NONE
    var opened = false
    var nextToFairy: Boolean? = null
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> config.colorBloodDoor
            DoorType.ENTRANCE -> config.colorEntranceDoor
            DoorType.WITHER -> if (opened) config.colorOpenWitherDoor else config.colorWitherDoor
            else -> config.colorRoomDoor
        }
}
