package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.Room
import funnymap.features.dungeon.ScanUtils.getRoomFromPos
import funnymap.utils.Utils.modMessage
import net.minecraft.tileentity.TileEntityChest

object MimicDetector {
    fun findMimic() {
        val mimicRoom = getMimicRoom()
        if (mimicRoom == "") return
        modMessage("Mimic found in $mimicRoom")
        Dungeon.dungeonList.forEach {
            if (it is Room && it.data.name == mimicRoom) {
                it.hasMimic = true
            }
        }
        Dungeon.mimicFound = true
    }

    private fun getMimicRoom(): String {
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }
            .groupingBy { getRoomFromPos(it.pos)?.data?.name }.eachCount().forEach { (room, trappedChests) ->
                Dungeon.uniqueRooms.find { it.data.name == room && it.data.trappedChests < trappedChests }
                    ?.let { return it.data.name }
            }
        return ""
    }
}
