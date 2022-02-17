package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.inDungeons
import funnymap.core.Door
import funnymap.core.DungeonPlayer
import funnymap.core.Room
import funnymap.core.Tile
import funnymap.utils.Utils
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent


class Dungeon {

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !inDungeons) return
        // Auto Scan every 250ms
        if (config.autoScan && System.currentTimeMillis() - lastScanTime >= 250) {
            if (!isScanning && !hasScanned && Utils.currentFloor != null) {
                lastScanTime = System.currentTimeMillis()
                Thread {
                    isScanning = true
                    DungeonScan.scanDungeon()
                    isScanning = false
                }.start()
            }
        }
        if (dungeonStarted && dungeonTeamates.isEmpty()) {
            MapUpdate.getPlayers()
        }
        if (hasScanned) {
            Thread {
                MapUpdate.updateRooms()
                MapUpdate.updatePlayers()
            }.start()
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2 || inBoss) return
        val message = StringUtils.stripControlCodes(event.message.unformattedText)
        if (entryMessages.any { it == message }) {
            inBoss = true
        } else if (message == "[NPC] Mort: Here, I found this map when I first entered the dungeon.") {
            dungeonStarted = true
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        reset()
        hasScanned = false
        inBoss = false
    }

    companion object {

        fun reset() {
            dungeonTeamates.clear()

            dungeonList.fill(Door(0, 0))
            uniqueRooms.clear()
            rooms.clear()

            puzzles.clear()
            trapType = ""
            witherDoors = 0
            secretCount = 0
        }

        const val roomSize = 32
        const val startX = 15
        const val startZ = 15

        /**
         * These variables aren't used yet but will probably be used later to determine the rendering size of the map
         * Floor 1, 2, 3: endX = 158 endZ = 158
         * Floor 4: endX = 190 endZ = 158
         * Floor 5, 6, 7: endX = 190 endZ = 190
         */
        private const val endX = 190
        private const val endZ = 190

        var lastScanTime: Long = 0
        var isScanning = false
        var hasScanned = false

        // 6 x 6 room grid, 11 x 11 with connections
        var dungeonStarted = false
        var inBoss = false
        val dungeonList = Array<Tile>(121) { Door(0, 0) }
        val uniqueRooms = mutableListOf<Room>()
        val rooms = mutableListOf<Room>()

        val dungeonTeamates = mutableListOf<DungeonPlayer>()

        // Used for chat info
        val puzzles = mutableListOf<String>()
        var trapType = ""
        var witherDoors = 0
        var secretCount = 0


        val entryMessages = listOf(
            "[BOSS] Bonzo: Gratz for making it this far, but I’m basically unbeatable.",
            "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
            "[BOSS] The Professor: I was burdened with terrible news recently...",
            "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
            "[BOSS] Livid: Welcome, you arrive right on time. I am Livid, the Master of Shadows.",
            "[BOSS] Sadan: So you made it all the way here...and you wish to defy me? Sadan?!",
            "[BOSS] Necron: Finally, I heard so much about you. The Eye likes you very much."
        )
    }
}
