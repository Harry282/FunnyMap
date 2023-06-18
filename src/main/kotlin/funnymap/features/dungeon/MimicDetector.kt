package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.map.Room
import funnymap.features.dungeon.ScanUtils.getRoomFromPos
import net.minecraft.tileentity.TileEntityChest

object MimicDetector {
    fun findMimic(): String? {
        val mimicRoom = getMimicRoom()
        if (mimicRoom == "") return null
        Dungeon.Info.dungeonList.forEach {
            if (it is Room && it.data.name == mimicRoom) {
                it.hasMimic = true
            }
        }
        return mimicRoom
    }

    private fun getMimicRoom(): String {
        if (ScoreCalc.higherFloor) {
            mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }
                .groupingBy { getRoomFromPos(it.pos)?.data?.name }.eachCount().forEach { (room, trappedChests) ->
                    Dungeon.Info.uniqueRooms.find { it.data.name == room && it.data.trappedChests < trappedChests }
                        ?.let { return it.data.name }
                }
        }
        return ""
    }
}
