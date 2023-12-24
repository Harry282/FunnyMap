package funnymap.core.map

import funnymap.FunnyMap.Companion.config
import java.awt.Color

class Door(override val x: Int, override val z: Int, var type: DoorType) : Tile {
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> config.colorBloodDoor
            DoorType.ENTRANCE -> config.colorEntranceDoor
            DoorType.WITHER -> if (opened) config.colorOpenWitherDoor else config.colorWitherDoor
            else -> config.colorRoomDoor
        }
}
