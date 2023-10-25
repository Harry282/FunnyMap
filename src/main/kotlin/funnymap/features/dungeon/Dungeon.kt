package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.FunnyMap.Companion.scope
import funnymap.core.DungeonPlayer
import funnymap.core.map.Door
import funnymap.core.map.Room
import funnymap.core.map.Tile
import funnymap.core.map.Unknown
import funnymap.events.ChatEvent
import funnymap.utils.Location.currentRoom
import funnymap.utils.Location.dungeonFloor
import funnymap.utils.Location.inBoss
import funnymap.utils.Location.inDungeons
import funnymap.utils.MapUtils
import funnymap.utils.Scoreboard
import funnymap.utils.TabList
import funnymap.utils.Utils.equalsOneOf
import gg.essential.universal.UChat
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.roundToInt

object Dungeon {
    private val secretPattern = Regex("(?<found>\\d+)/(?<total>\\d+) Secrets")
    private val leverPattern = Regex("§r§\\S(?<player>\\S+)§r§a activated a lever! \\(§r§c\\d§r§a/\\d\\)§r")
    private val terminalPattern = Regex("§r§\\S(?<player>\\S+)§r§a activated a terminal! \\(§r§c\\d§r§a/\\d\\)§r")
    private val devicePattern = Regex("§r§\\S(?<player>\\S+)§r§a completed a device! \\(§r§c\\d§r§a/\\d\\)§r")

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
            }
        }

        if (!MapUtils.calibrated) {
            MapUtils.calibrated = MapUtils.calibrateMap()
        }

        dungeonTeammates[mc.thePlayer.name]?.apply {
            yaw = mc.thePlayer.rotationYawHead
            mapX =
                ((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
            mapZ =
                ((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
        }

        MapUpdate.updateRooms()
        MapUpdate.updateDoors()
        MapUpdate.updateDecor()
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (!inDungeons) return
        if (event.packet.type.toInt() == 2) {
            if (currentRoom == null || inBoss) return
            secretPattern.find(event.text)?.let {
                val secrets = it.groups["found"]?.value?.toIntOrNull() ?: return
                val total = it.groups["total"]?.value?.toIntOrNull() ?: return
                if (total != currentRoom?.data?.secrets) return
                if (currentRoom?.secretsfound != secrets) {
                    currentRoom?.secretsfound = secrets
                    Info.uniqueRooms.find { it.data.name == currentRoom?.data?.name }?.secretsfound = secrets
                    ScoreCalc.calcScore()
                }
            }
        } else {
            if (config.terminalInfo && event.formatted == "§r§aThe Core entrance is opening!§r" && dungeonFloor == 7) {
                PlayerTracker.onTerminalPhaseEnd()
            } else if (event.packet.chatComponent.siblings.any {
                    it.chatStyle?.chatClickEvent?.run {
                        action == ClickEvent.Action.RUN_COMMAND && value == "/showextrastats"
                    } == true
                }) {
                if (config.teamInfo) {
                    PlayerTracker.onDungeonEnd()
                }
            }
            when (event.text) {
                "Starting in 4 seconds." -> MapUpdate.preloadHeads()
                "[NPC] Mort: Here, I found this map when I first entered the dungeon." -> {
                    MapUpdate.getPlayers()
                    Info.startTime = System.currentTimeMillis()
                }

                else -> {
                    leverPattern.find(event.formatted)?.let {
                        it.groups["player"]?.value?.run {
                            dungeonTeammates.getOrElse(this) { return }.levers++
                        }
                    } ?: terminalPattern.find(event.formatted)?.let {
                        it.groups["player"]?.value?.run {
                            dungeonTeammates.getOrElse(this) { return }.terminals++
                        }
                    } ?: devicePattern.find(event.formatted)?.let {
                        it.groups["player"]?.value?.run {
                            dungeonTeammates.getOrElse(this) { return }.devices++
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
        TabList.TabList.clear()
        Scoreboard.tries = 0
    }

    fun reset() {
        Info.reset()
        dungeonTeammates.clear()
        PlayerTracker.roomClears.clear()
        MapUtils.calibrated = false
        DungeonScan.hasScanned = false
    }

    private fun shouldSearchMimic() = DungeonScan.hasScanned && MimicDetector.mimicRoom == null && dungeonFloor.equalsOneOf(6, 7)

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
        var rooms = 0
        val dungeonList = Array<Tile>(121) { Unknown(0, 0) }
        val uniqueRooms = mutableListOf<Room>()
        val puzzles = mutableListOf<String>()

        var trapType = ""
        var witherDoors = 0
        var fairyOpened = false
        var fairyPos: Pair<Int, Int>? = null
        var cryptCount = 0
        var secretCount = 0

        var startTime = 0L
        fun reset() {
            rooms = 0
            dungeonList.fill(Unknown(0, 0))
            uniqueRooms.clear()
            puzzles.clear()

            trapType = ""
            witherDoors = 0
            fairyOpened = false
            fairyPos = null
            cryptCount = 0
            secretCount = 0

            startTime = 0L
        }
    }
}
