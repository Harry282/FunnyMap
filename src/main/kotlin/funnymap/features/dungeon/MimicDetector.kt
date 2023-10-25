package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.features.dungeon.ScanUtils.getRoomFromPos
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos

object MimicDetector {
    var mimicRoom: String? = null
    var mimicPos: BlockPos? = null

    fun findMimic(): String? {
        try {
            mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }
                .groupBy { getRoomFromPos(Pair(it.pos.x, it.pos.z))?.data?.name }.forEach { (room, trappedChests) ->
                    Dungeon.Info.uniqueRooms.find { it.data.name == room && it.data.trappedChests < trappedChests.size }
                        ?.let {
                            mimicRoom = it.data.name
                            mimicPos = trappedChests[0].pos //May be inaccurate (when trappedChests.size > 0)
                            return it.data.name
                        }
                }
            return null
        } catch (_: ConcurrentModificationException) { return null }
    }
}
