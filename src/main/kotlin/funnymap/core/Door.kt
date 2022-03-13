package funnymap.core

import funnymap.FunnyMap.Companion.config
import java.awt.Color

data class Door(override var x: Int, override var z: Int) : Tile(x, z) {

    var type = DoorType.NONE
    var opened = false

    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> config.colorBloodDoor
            DoorType.ENTRANCE -> config.colorEntranceDoor
            DoorType.WITHER -> if (opened) config.colorOpenWitherDoor else config.colorWitherDoor
            else -> config.colorRoomDoor
        }
}
