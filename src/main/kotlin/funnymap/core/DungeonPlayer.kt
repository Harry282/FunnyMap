package funnymap.core

import funnymap.FunnyMap.Companion.mc
import net.minecraft.entity.player.EntityPlayer

class DungeonPlayer(val player: EntityPlayer, val name: String) {
    var dead = false
    var skin = mc.netHandler.getPlayerInfo(player.uniqueID).locationSkin
}
