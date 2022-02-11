package funnymap.utils

import funnymap.FunnyMap.Companion.mc
import net.minecraft.item.ItemMap
import net.minecraft.world.storage.MapData

object MapUtils {

    var startCorner = Pair(5, 5)
    var roomSize = 16
    var calibrated = false

    fun getMapData(): MapData? {
        val map = mc.thePlayer?.inventory?.getStackInSlot(8) ?: return null
        if (map.item !is ItemMap || !map.displayName.contains("Magical Map")) return null
        return (map.item as ItemMap).getMapData(map, mc.theWorld)
    }
}
