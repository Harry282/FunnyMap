package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.core.DungeonPlayer
import funnymap.core.map.*
import funnymap.events.ChatEvent
import funnymap.utils.Location
import funnymap.utils.Location.inDungeons
import funnymap.utils.MapUtils
import funnymap.utils.TabList
import funnymap.utils.Utils.equalsOneOf
import gg.essential.universal.UChat
import net.minecraft.event.ClickEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Dungeon {

    val dungeonTeammates = mutableMapOf<String, DungeonPlayer>()
    val espDoors = mutableListOf<Door>()

    fun onTick() {
        if (!inDungeons) return

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
        MimicDetector.checkMimicDead()
        ScoreCalculation.updateScore()

        TabList.getDungeonTabList()?.let {
            MapUpdate.updatePlayers(it)
            RunInformation.updatePuzzleCount(it)
        }

        if (DungeonScan.shouldScan) {
//            scope.launch { DungeonScan.scan() }
            DungeonScan.scan()
        }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (event.packet.type.toInt() == 2 || !inDungeons) return
        if (event.packet.chatComponent.siblings.any {
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

            "[BOSS] The Watcher: You have proven yourself. You may pass." -> RunInformation.bloodDone = true
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
        RunInformation.reset()
    }

    fun shouldSearchMimic() = DungeonScan.hasScanned && !Info.mimicFound && Location.dungeonFloor.equalsOneOf(6, 7)

    object Info {
        // 6 x 6 room grid, 11 x 11 with connections
        val dungeonList = Array<Tile>(121) { Unknown(0, 0) }
        val uniqueRooms = mutableListOf<Pair<Room, Pair<Int, Int>>>()
        var roomCount = 0
        val puzzles = mutableMapOf<Puzzle, Boolean>()

        var trapType = ""
        var witherDoors = 0
        var cryptCount = 0
        var secretCount = 0
        var mimicFound = false

        var startTime = 0L
        fun reset() {
            dungeonList.fill(Unknown(0, 0))
            roomCount = 0
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
