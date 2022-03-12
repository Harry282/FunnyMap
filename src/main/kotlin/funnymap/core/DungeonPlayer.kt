package funnymap.core

import net.minecraft.entity.player.EntityPlayer

data class DungeonPlayer(val player: EntityPlayer, val name: String) {
    var mapX = 0.0
    var mapZ = 0.0
    var yaw = 0f
    var icon = ""
    var dead = false
}
