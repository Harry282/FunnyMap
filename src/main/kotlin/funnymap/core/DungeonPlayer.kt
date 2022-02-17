package funnymap.core

import net.minecraft.entity.player.EntityPlayer

class DungeonPlayer(val player: EntityPlayer, val name: String) {
    var x = 0.0
    var z = 0.0
    var yaw = 0f
    var icon = ""
    var dead = false
}
