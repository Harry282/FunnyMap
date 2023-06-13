package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.map.Room
import funnymap.features.dungeon.ScanUtils.getRoomFromPos
import net.minecraft.tileentity.TileEntityChest

object MimicDetector {
    var roomName: String? = null

    fun findMimic() {
        if (roomName != null) return
        roomName = getMimicRoom() ?: return
        Dungeon.Info.dungeonList.forEach {
            if (it is Room && it.data.name == roomName) {
                it.hasMimic = true
            }
        }
    }

    private fun getMimicRoom(): String? {
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }
            .groupingBy { getRoomFromPos(it.pos)?.data?.name }.eachCount().forEach { (room, trappedChests) ->
                Dungeon.Info.uniqueRooms.toList().find { it.data.name == room && it.data.trappedChests < trappedChests }
                    ?.let { return it.data.name }
            }
        return null
    }
}
