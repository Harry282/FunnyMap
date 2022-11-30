package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.core.map.*
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapX
import funnymap.utils.MapUtils.mapZ
import funnymap.utils.MapUtils.yaw
import funnymap.utils.Utils
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils

object MapUpdate {
    fun preloadHeads() {
        val tabEntries = Utils.dungeonTabList ?: return
        for (i in listOf(5, 9, 13, 17, 1)) {
            // Accessing the skin locations to load in skin
            tabEntries[i].first.locationSkin
        }
    }

    fun getPlayers() {
        val tabEntries = Utils.dungeonTabList ?: return
        Dungeon.dungeonTeammates.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with(tabEntries[i]) {
                val name = StringUtils.stripControlCodes(second).trim().substringAfterLast("] ").split(" ")[0]
                if (name != "") {
                    Dungeon.dungeonTeammates[name] = DungeonPlayer(first.locationSkin).apply {
                        icon = "icon-$iconNum"
                        renderHat = mc.theWorld.getPlayerEntityByName(name)?.isWearing(EnumPlayerModelParts.HAT) == true
                    }
                    iconNum++
                }
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return
        // Update map icons
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val name = tabText.substringAfterLast("] ").split(" ")[0]
            if (name == "") continue
            Dungeon.dungeonTeammates[name]?.run {
                dead = tabText.contains("(DEAD)")
                if (dead) {
                    icon = ""
                } else {
                    icon = "icon-$iconNum"
                    iconNum++
                }
            }
        }

        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        Dungeon.dungeonTeammates.forEach { (name, player) ->
            if (name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYawHead
            } else {
                decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                    player.mapX = vec4b.mapX
                    player.mapZ = vec4b.mapZ
                    player.yaw = vec4b.yaw
                }
            }
        }
    }

    fun updateRooms() {
        val mapColors = MapUtils.getMapData()?.colors ?: return

        val startX = MapUtils.startCorner.first + (MapUtils.mapRoomSize shr 1)
        val startZ = MapUtils.startCorner.second + (MapUtils.mapRoomSize shr 1)
        val increment = (MapUtils.mapRoomSize shr 1) + 2

        for (x in 0..10) {
            for (z in 0..10) {

                val mapX = startX + x * increment
                val mapZ = startZ + z * increment

                if (mapX >= 128 || mapZ >= 128) continue

                val room = Dungeon.Info.dungeonList[z * 11 + x]

                room.state = when (mapColors[(mapZ shl 7) + mapX].toInt()) {
                    0, 85, 119 -> RoomState.UNDISCOVERED
                    18 -> if (room is Room) when (room.data.type) {
                        RoomType.BLOOD -> RoomState.DISCOVERED
                        RoomType.PUZZLE -> RoomState.FAILED
                        else -> room.state
                    } else RoomState.DISCOVERED

                    30 -> if (room is Room) when (room.data.type) {
                        RoomType.ENTRANCE -> RoomState.DISCOVERED
                        else -> RoomState.GREEN
                    } else room.state

                    34 -> RoomState.CLEARED
                    else -> RoomState.DISCOVERED
                }
            }
        }
    }

    fun updateDoors() {
        Dungeon.Info.dungeonList.filterIsInstance<Door>().forEach { door ->
            if (!door.opened && door.type == DoorType.WITHER && mc.theWorld.getChunkFromChunkCoords(
                    door.x shr 4, door.z shr 4
                ).isLoaded
            ) {
                if (mc.theWorld.getBlockState(BlockPos(door.x, 69, door.z)).block == Blocks.air) {
                    door.opened = true
                }
            }
        }
    }
}
