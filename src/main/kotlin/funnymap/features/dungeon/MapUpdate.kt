package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.core.map.Door
import funnymap.core.map.DoorType
import funnymap.core.map.Unknown
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapX
import funnymap.utils.MapUtils.mapZ
import funnymap.utils.MapUtils.yaw
import funnymap.utils.TabList
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import kotlin.math.roundToInt

object MapUpdate {
    fun preloadHeads() {
        val tabEntries = TabList.getDungeonTabList() ?: return
        for (i in listOf(5, 9, 13, 17, 1)) {
            // Accessing the skin locations to load in skin
            tabEntries[i].first.locationSkin
        }
    }

    fun getPlayers() {
        val tabEntries = TabList.getDungeonTabList() ?: return
        Dungeon.dungeonTeammates.clear()
        var iconNum = 0
        for (i in listOf(5, 9, 13, 17, 1)) {
            with(tabEntries[i]) {
                val name = StringUtils.stripControlCodes(second).trim().substringAfterLast("] ").split(" ")[0]
                if (name != "") {
                    Dungeon.dungeonTeammates[name] = DungeonPlayer(first.locationSkin).apply {
                        mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                        colorPrefix = second.substringBefore(name, "f").last()
                        this.name = name
                        icon = "icon-$iconNum"
                    }
                    iconNum++
                }
            }
        }
    }

    fun updatePlayers(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return
        // Update map icons
        val time = System.currentTimeMillis() - Dungeon.Info.startTime
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
                if (!playerLoaded) {
                    mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                }

                val room = getCurrentRoom()
                if (room != "Error" || time > 1000) {
                    if (lastRoom == "") {
                        lastRoom = room
                    } else if (lastRoom != room) {
                        roomVisits.add(Pair(time - lastTime, lastRoom))
                        lastTime = time
                        lastRoom = room
                    }
                }
            }
        }

        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        Dungeon.dungeonTeammates.forEach { (name, player) ->
            decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                player.isPlayer = vec4b.func_176110_a().toInt() == 1
                player.mapX = vec4b.mapX
                player.mapZ = vec4b.mapZ
                player.yaw = vec4b.yaw
            }
            if (player.isPlayer || name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX =
                    ((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ =
                    ((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }
    }

    fun updateRooms() {
        val map = DungeonMap(MapUtils.getMapData()?.colors ?: return)
        Dungeon.espDoors.clear()

        for (x in 0..10) {
            for (z in 0..10) {
                val room = Dungeon.Info.dungeonList[z * 11 + x]
                val mapTile = map.getTile(x, z)

                if (room is Unknown) {
                    Dungeon.Info.dungeonList[z * 11 + x] = mapTile
                    continue
                }

                if (mapTile.state.ordinal < room.state.ordinal) {
                    PlayerTracker.roomStateChange(room, room.state, mapTile.state)
                    room.state = mapTile.state
                }

                if (room is Door && room.type.equalsOneOf(DoorType.ENTRANCE, DoorType.WITHER, DoorType.BLOOD)) {
                    if (mapTile is Door && mapTile.type == DoorType.WITHER) {
                        room.opened = false
                    } else if (!room.opened && mc.theWorld.getChunkFromChunkCoords(
                            room.x shr 4,
                            room.z shr 4
                        ).isLoaded
                    ) {
                        if (mc.theWorld.getBlockState(BlockPos(room.x, 69, room.z)).block == Blocks.air) {
                            room.opened = true
                        }
                    }

                    if (!room.opened) {
                        Dungeon.espDoors.add(room)
                    }
                }
            }
        }
    }
}
