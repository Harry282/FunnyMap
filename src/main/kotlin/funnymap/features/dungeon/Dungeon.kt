package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.scope
import funnymap.core.DungeonPlayer
import funnymap.core.map.Door
import funnymap.core.map.Room
import funnymap.core.map.Tile
import funnymap.core.map.Unknown
import funnymap.events.ChatEvent
import funnymap.utils.LocationUtils
import funnymap.utils.LocationUtils.inDungeons
import funnymap.utils.MapUtils
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import gg.essential.universal.UChat
import kotlinx.coroutines.launch
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Dungeon {

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()
    val espDoors = mutableListOf<Door>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return

        if (DungeonScan.shouldScan) {
            scope.launch { DungeonScan.scan() }
        }

        if (DungeonScan.isScanning) return

        if (shouldSearchMimic()) {
            MimicDetector.findMimic()?.let {
                UChat.chat("&7Mimic Room: &c$it")
                Info.mimicFound = true
            }
        }

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

    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (event.packet.type.toInt() == 2 || !inDungeons) return
        when (event.text) {
            "Starting in 4 seconds." -> MapUpdate.preloadHeads()
            "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                MapUpdate.getPlayers()
                Info.startTime = System.currentTimeMillis()
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

    fun shouldSearchMimic() = DungeonScan.hasScanned && !Info.mimicFound && LocationUtils.dungeonFloor.equalsOneOf(6, 7)

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
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
