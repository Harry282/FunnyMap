package funnymap.utils

import funnymap.FunnyMap.Companion.mc
import net.minecraft.item.ItemMap
import net.minecraft.util.Vec4b
import net.minecraft.world.storage.MapData

object MapUtils {

    var startCorner = Pair(5, 5)
    var roomSize = 16
    var calibrated = false
    var coordMultiplier = 0.5

    fun getMapData(): MapData? {
        val map = mc.thePlayer?.inventory?.getStackInSlot(8) ?: return null
        if (map.item !is ItemMap || !map.displayName.contains("Magical Map")) return null
        return (map.item as ItemMap).getMapData(map, mc.theWorld)
    }

    val Vec4b.mapX
        get() = (this.func_176112_b() + 128) shr 1

    val Vec4b.mapZ
        get() = (this.func_176113_c() + 128) shr 1

    val Vec4b.yaw
        get() = this.func_176111_d() * 22.5f
}
