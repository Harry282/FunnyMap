package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.*
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapX
import funnymap.utils.MapUtils.mapZ
import funnymap.utils.MapUtils.yaw
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
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

        MapUtils.coordMultiplier = (MapUtils.roomSize + 4.0) / Dungeon.roomSize

        MapUtils.calibrated = true
    }

    fun getPlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isNotEmpty()) return

        for (i in 0..4) {
            val text = StringUtils.stripControlCodes(tabEntries[1 + i * 4].second).trim()
            val name = text.split(" ")[0]
            if (name == "") continue
            mc.theWorld.playerEntities.find { it.name == name }?.let {
                Dungeon.dungeonTeammates.add(DungeonPlayer(it, name))
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return

        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            val tabText = StringUtils.stripControlCodes(tabEntries[i].second).trim()
            val name = tabText.split(" ")[0]
            if (name == "") continue
            val player = Dungeon.dungeonTeammates.find { it.name == name } ?: continue
            player.dead = tabText.contains("(DEAD)")
            if (!player.dead) {
                player.icon = "icon-${iconNum}"
                iconNum++
            } else {
                player.icon = ""
            }
        }

        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        Dungeon.dungeonTeammates.forEach {
            if (it.player == mc.thePlayer) {
                it.yaw = it.player.rotationYawHead
            } else {
                decor.entries.find { (icon, _) -> icon == it.icon }?.let { (_, vec4b) ->
                    it.mapX = vec4b.mapX.toDouble()
                    it.mapZ = vec4b.mapZ.toDouble()
                    it.yaw = vec4b.yaw
                }
            }
        }
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

                if (mapX >= 128 || mapZ >= 128) continue

                val room = Dungeon.dungeonList[z * 11 + x]

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
        for ((door, pos) in Dungeon.doors) {
            if (!door.opened && mc.theWorld.getChunkFromChunkCoords(door.x shr 4, door.z shr 4).isLoaded) {
                if (mc.theWorld.getBlockState(BlockPos(door.x, 69, door.z)).block == Blocks.air) {
                    val room = Dungeon.dungeonList[pos.first + pos.second * 11]
                    if (room is Door && room.type == DoorType.WITHER) {
                        room.opened = true
                        door.opened = true
                    }
                }
            }
        }
    }
}
