package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.scope
import funnymap.core.DungeonPlayer
import funnymap.core.map.Room
import funnymap.core.map.Tile
import funnymap.core.map.Unknown
import funnymap.events.ChatEvent
import funnymap.utils.LocationUtils.currentRoom
import funnymap.utils.LocationUtils.inDungeons
import funnymap.utils.MapUtils
import funnymap.utils.Utils
import kotlinx.coroutines.launch
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Pattern

object Dungeon {

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return
        if (DungeonScan.shouldScan) {
            scope.launch { DungeonScan.scan() }
        }
        if (!DungeonScan.isScanning) {
            if (!MapUtils.calibrated) {
                MapUtils.calibrated = MapUtils.calibrateMap()
            }
            MapUpdate.updateRooms()
            MapUpdate.updateDoors()
            Utils.dungeonTabList?.let {
                MapUpdate.updatePlayers(it)
                RunInformation.updateRunInformation(it)
            }
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (!inDungeons) return
        if (event.packet.type.toInt() == 2) {
            if (currentRoom == null) return
            val regex = Pattern.compile("(?<found>\\d+)/(?<total>\\d+) Secrets").matcher(event.text)
            if (regex.find()) {
                val secrets = regex.group("found").toInt()
                val total = regex.group("total").toInt()
                if (total != currentRoom?.data?.secrets) return
                if (currentRoom?.secretsfound != secrets) {
                    currentRoom?.secretsfound = secrets
                    val room = Info.uniqueRooms.find { it.data.name == currentRoom?.data?.name } ?: return
                    Info.uniqueRooms[Info.uniqueRooms.indexOf(room)].secretsfound = secrets
                    ScoreCalc.calcScore()
                }
            }
        } else {
            when (event.text) {
                "Dungeon starts in 4 seconds.", "Dungeon starts in 4 seconds. Get ready!" -> MapUpdate.preloadHeads()
                "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                    MapUpdate.getPlayers()
                    Info.startTime = System.currentTimeMillis()
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
    }

    fun reset() {
        Info.reset()
        dungeonTeammates.clear()
        PlayerTracker.roomClears.clear()
        MapUtils.calibrated = false
        DungeonScan.hasScanned = false
    }

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
        var rooms = 0
        val dungeonList = Array<Tile>(121) { Unknown(0, 0) }
        val uniqueRooms = mutableListOf<Room>()
        val puzzles = mutableListOf<String>()

        var trapType = ""
        var witherDoors = 0
        var cryptCount = 0
        var secretCount = 0
        var mimicFound = false

        var startTime = 0L
        fun reset() {
            rooms = 0
            dungeonList.fill(Unknown(0, 0))
            uniqueRooms.clear()
            puzzles.clear()

            trapType = ""
            witherDoors = 0
            cryptCount = 0
            secretCount = 0
            mimicFound = false

            startTime = 0L
        }
    }
}
