package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.core.map.*
import funnymap.features.dungeon.RunInformation.firstDeath
import funnymap.utils.LocationUtils.started
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapX
import funnymap.utils.MapUtils.mapZ
import funnymap.utils.MapUtils.yaw
import funnymap.utils.Utils
import net.minecraft.client.network.NetworkPlayerInfo
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
                    if (!firstDeath && started > 0) {
                        if (spiritPet) {
                            ScoreCalc.firstDeathHadSpirit = true
                        }
                        firstDeath = true
                    }
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
            if (name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYawHead
                player.mapX =
                    (MapUtils.startCorner.first - 2 + (mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier).toInt()
                player.mapZ =
                    (MapUtils.startCorner.second - 2 + (mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier).toInt()
                return@forEach
            }
            decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                player.mapX = vec4b.mapX
                player.mapZ = vec4b.mapZ
                player.yaw = vec4b.yaw
            }
        }
    }

    fun updateRooms() {
        val mapColors = MapUtils.getMapData()?.colors ?: return

        val startX = MapUtils.startCorner.first + (MapUtils.mapRoomSize shr 1)
        val startZ = MapUtils.startCorner.second + (MapUtils.mapRoomSize shr 1)
        val increment = (MapUtils.mapRoomSize shr 1) + 2
        var changed = false

        for (x in 0..10) {
            for (z in 0..10) {

                val mapX = startX + x * increment
                val mapZ = startZ + z * increment

                if (mapX >= 128 || mapZ >= 128) continue

                val room = Dungeon.Info.dungeonList[z * 11 + x]

                val newState = when (mapColors[(mapZ shl 7) + mapX].toInt()) {
                    0, 85, 119 -> RoomState.UNDISCOVERED
                    18 -> if (room is Room) when (room.data.type) {
                        RoomType.BLOOD -> RoomState.DISCOVERED
                        RoomType.PUZZLE -> RoomState.FAILED
                        else -> room.state
                    } else RoomState.DISCOVERED

                    30 -> if (room is Room) when (room.data.type) {
                        RoomType.ENTRANCE -> RoomState.DISCOVERED
                        else -> {
                            RoomState.GREEN.apply { if (room.hasMimic) ScoreCalc.mimicKilled = true }
                        }
                    } else room.state

                    34 -> RoomState.CLEARED
                    else -> RoomState.DISCOVERED
                }

                if (newState != room.state) {
                    changed = true
                    PlayerTracker.roomStateChange(room, room.state, newState)
                    room.state = newState
                    if (room is Room && newState == RoomState.GREEN) room.secretsfound = room.data.secrets
                }
            }
        }
        if (changed) ScoreCalc.calcScore()
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