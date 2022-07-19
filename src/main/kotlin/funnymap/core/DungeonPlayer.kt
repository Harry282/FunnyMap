package funnymap.core

import net.minecraft.util.ResourceLocation

data class DungeonPlayer(val skin: ResourceLocation) {
    var mapX = 0
    var mapZ = 0
    var yaw = 0f
    var renderHat = false
    var icon = ""
    var dead = false
}
