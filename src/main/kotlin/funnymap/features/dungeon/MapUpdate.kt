package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.core.Room
import funnymap.core.RoomState
import funnymap.core.RoomType
import funnymap.utils.MapUtils
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.util.StringUtils

object MapUpdate {
    fun calibrate() {
        MapUtils.startCorner = when {
            Utils.currentFloor == 1 -> Pair(22, 11)
            Utils.currentFloor.equalsOneOf(2, 3) -> Pair(11, 11)
            Utils.currentFloor == 4 && Dungeon.rooms.size > 25 -> Pair(5, 16)
            Dungeon.rooms.size == 30 -> Pair(16, 5)
            Dungeon.rooms.size == 25 -> Pair(11, 11)
            else -> Pair(5, 5)
        }

        MapUtils.roomSize = if (Utils.currentFloor in 1..3 || Dungeon.rooms.size == 24) 18 else 16

        MapUtils.calibrated = true
    }

    fun getPlayers() {
        if (Dungeon.dungeonTeamates.isNotEmpty()) return
        val tabEntries = getDungeonTabList() ?: return

        for (i in 0..4) {
            val text = StringUtils.stripControlCodes(tabEntries[1 + i * 4].second).trim()
            val name = text.split(" ")[0]
            if (name == "") continue
            mc.theWorld.playerEntities.find { it.name == name }?.let {
                Dungeon.dungeonTeamates.add(DungeonPlayer(it, name))
            }
        }
    }

    private fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        val tabEntries = Utils.tabList
        if (tabEntries.isEmpty() || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            return null
        }
        return tabEntries
    }

    fun updateRooms() {
        val mapColors = MapUtils.getMapData()?.colors ?: return

        val startX = MapUtils.startCorner.first + (MapUtils.roomSize shr 1)
        val startZ = MapUtils.startCorner.second + (MapUtils.roomSize shr 1)
        val increment = (MapUtils.roomSize shr 1) + 2

        for (x in 0..10) {
            for (z in 0..10) {

                val mapX = startX + x * increment
                val mapZ = startZ + z * increment

                val room = Dungeon.dungeonList[z * 11 + x]

                when (mapColors[(mapZ shl 7) + mapX].toInt()) {
                    0, 85, 119 -> room.state = RoomState.UNDISCOVERED
                    18 -> if (room is Room) when (room.data.type) {
                        RoomType.BLOOD -> room.state = RoomState.DISCOVERED
                        RoomType.PUZZLE -> room.state = RoomState.FAILED
                        else -> {}
                    }
                    30 -> if (room is Room) when (room.data.type) {
                        RoomType.ENTRANCE -> room.state = RoomState.DISCOVERED
                        else -> room.state = RoomState.GREEN
                    }
                    34 -> room.state = RoomState.CLEARED
                    else -> room.state = RoomState.DISCOVERED
                }
            }
        }
    }
}
