package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.scope
import funnymap.core.Door
import funnymap.core.DungeonPlayer
import funnymap.core.Room
import funnymap.core.Tile
import funnymap.events.ChatEvent
import funnymap.utils.LocationUtils.dungeonFloor
import funnymap.utils.LocationUtils.inBoss
import funnymap.utils.LocationUtils.inDungeons
import funnymap.utils.MapUtils
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import kotlinx.coroutines.launch
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Dungeon {

    const val roomSize = 32
    const val startX = -185
    const val startZ = -185

    private var lastScanTime: Long = 0
    private var isScanning = false
    var hasScanned = false

    // 6 x 6 room grid, 11 x 11 with connections
    val dungeonList = Array<Tile>(121) { Door(0, 0) }
    val uniqueRooms = mutableListOf<Room>()
    val rooms = mutableListOf<Room>()
    val doors = mutableMapOf<Door, Pair<Int, Int>>()
    var mimicFound = false

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()

    // Used for chat info
    val puzzles = mutableListOf<String>()
    var trapType = ""
    var witherDoors = 0
    var secretCount = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return
        scope.launch {
            if (shouldScan()) {
                lastScanTime = System.currentTimeMillis()
                isScanning = true
                DungeonScan.scanDungeon()
                isScanning = false
            }
        }
        getDungeonTabList()?.let {
            MapUpdate.updatePlayers(it)
            RunInformation.updateRunInformation(it)
        }
        if (hasScanned) {
            if (!mimicFound && dungeonFloor.equalsOneOf(6, 7)) {
                MimicDetector.findMimic()
            }
            MapUpdate.updateRooms()
            MapUpdate.updateDoors()
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (event.packet.type.toInt() == 2 || !inDungeons) return
        when {
            event.text.equalsOneOf(
                "Dungeon starts in 4 seconds.", "Dungeon starts in 4 seconds. Get ready!"
            ) -> MapUpdate.preloadHeads()

            event.text == "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                MapUpdate.getPlayers()
                MapUtils.startCorner = when {
                    dungeonFloor == 1 -> Pair(22, 11)
                    dungeonFloor.equalsOneOf(2, 3) -> Pair(11, 11)
                    dungeonFloor == 4 -> Pair(5, 16)
                    else -> Pair(5, 5)
                }

                MapUtils.roomSize = if (dungeonFloor in 1..3) 18 else 16

                MapUtils.coordMultiplier = (MapUtils.roomSize + 4.0) / roomSize
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
        hasScanned = false
        inBoss = false
    }

    private fun shouldScan() =
        config.autoScan && !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && dungeonFloor != -1

    fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        val tabEntries = Utils.tabList
        if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            return null
        }
        return tabEntries
    }


    fun reset() {
        dungeonTeammates.clear()

        dungeonList.fill(Door(0, 0))
        uniqueRooms.clear()
        rooms.clear()
        doors.clear()
        mimicFound = false

        puzzles.clear()
        trapType = ""
        witherDoors = 0
        secretCount = 0
    }
}
