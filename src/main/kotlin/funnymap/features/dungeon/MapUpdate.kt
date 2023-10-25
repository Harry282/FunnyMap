package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.core.map.*
import funnymap.features.dungeon.Dungeon.Info.fairyOpened
import funnymap.features.dungeon.Dungeon.Info.fairyPos
import funnymap.features.dungeon.RunInformation.firstDeath
import funnymap.utils.Location.dungeonFloor
import funnymap.utils.Location.started
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapX
import funnymap.utils.MapUtils.mapZ
import funnymap.utils.MapUtils.yaw
import funnymap.utils.TabList
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import kotlin.math.abs

object MapUpdate {
    private val iconIndexes = listOf(5, 9, 13, 17, 1)
    fun preloadHeads() {
        if (TabList.checkTabList()) {
            val tabEntries = TabList.TabList.toMutableList()
            for (i in iconIndexes) {
                // Accessing the skin locations to load in skin
                tabEntries.getOrNull(i)?.locationSkin
            }
        }
    }

    fun getPlayers() {
        if (TabList.checkTabList()) {
            val tabEntries = TabList.TabList.toMutableList()
            Dungeon.dungeonTeammates.clear()
            var iconNum = 0
            for (i in iconIndexes) {
                tabEntries.getOrNull(i)?.let { player ->
                    val name =
                        player.displayName?.unformattedText?.let { it.trim().substringAfterLast("] ").split(" ")[0] }
                            ?: ""
                    if (name != "") {
                        player.locationSkin?.let { DungeonPlayer(it) }?.apply {
                            mc.theWorld.getPlayerEntityByName(name)?.let { setData(it) }
                            colorPrefix = player.displayName?.formattedText?.substringBefore(name, "f")?.last() ?: 'f'
                            this.name = name
                            icon = "icon-$iconNum"
                        }?.run { Dungeon.dungeonTeammates[name] = this }
                        iconNum++
                    }
                }
            }
        }
    }

    fun updatePlayer(tablist: List<NetworkPlayerInfo>) {
        if (Dungeon.dungeonTeammates.isEmpty()) return
        // Update map icons
        val time = System.currentTimeMillis() - Dungeon.Info.startTime
        var iconNum = 0
        for (i in iconIndexes) {
            val tabText = tablist[i].displayName?.unformattedText?.trim() ?: continue
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
    }

    fun updateDecor() {
        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        Dungeon.dungeonTeammates.forEach { (name, player) ->
            if (name == mc.thePlayer.name) return@forEach
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
                            RoomState.GREEN.apply {
                                if (room.data.name == MimicDetector.mimicRoom && dungeonFloor.equalsOneOf(6, 7) && !ScoreCalc.mimicKilled) {
                                    ScoreCalc.mimicKilled = true
                                    if (config.sendMimicFound) mc.thePlayer.sendChatMessage("/pc ${config.mimicMessage}")
                                    changed = true
                                }
                            }
                        }
                    } else room.state

                    34 -> RoomState.CLEARED
                    else -> RoomState.DISCOVERED
                }

                if (newState != room.state) {
                    changed = true
                    PlayerTracker.roomStateChange(room, room.state, newState)
                    room.state = newState
                    if (room is Room && newState == RoomState.GREEN) {
                        room.secretsfound = room.data.secrets
                        if (room.data.type == RoomType.FAIRY) fairyOpened = true
                    }
                }
            }
        }
        if (changed) ScoreCalc.calcScore()
    }

    fun updateDoors() {
        Dungeon.espDoors.clear()
        Dungeon.Info.dungeonList.filterIsInstance<Door>().forEach { door ->
            if (door.nextToFairy == null && !fairyOpened) {
                fairyPos?.run {
                    door.nextToFairy = (door.x == this.first && abs(door.z - this.second).shr(4) == 1) || (abs(door.x - this.first).shr(4) == 1 && door.z == this.second)
                }
            }
            val fairyDoor = door.nextToFairy == true && !fairyOpened
            if ((fairyDoor || !door.opened) && door.type.equalsOneOf(DoorType.WITHER, DoorType.BLOOD) &&
                mc.theWorld.getChunkFromChunkCoords(door.x shr 4, door.z shr 4).isLoaded
            ) {
                if (mc.theWorld.getBlockState(BlockPos(door.x, 69, door.z)).block == Blocks.air) {
                    door.opened = true
                    if (fairyDoor) Dungeon.espDoors.add(door)
                } else {
                    Dungeon.espDoors.add(door)
                }
            }
        }
    }
}
