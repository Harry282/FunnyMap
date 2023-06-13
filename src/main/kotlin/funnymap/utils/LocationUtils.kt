package funnymap.utils

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.RoomData
import funnymap.core.map.Room
import funnymap.core.map.RoomType
import funnymap.events.ChatEvent
import funnymap.features.dungeon.Dungeon
import funnymap.features.dungeon.Ghostblocks
import funnymap.features.dungeon.ScanUtils
import funnymap.features.dungeon.ScoreCalc
import funnymap.utils.ScoreboardUtils.sidebarLines
import funnymap.utils.Utils.equalsOneOf
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import kotlin.math.max
import kotlin.math.min

object LocationUtils {

    private var onHypixel = false
    var inSkyblock = false
    var inDungeons = false
    var started: Long = 0
    var dungeonFloor = -1
    var masterMode = false
    var inBoss = false
    var currentRoom: Room? = null

    private var tickCount = 0

    private val entryMessages = listOf(
        "[BOSS] Bonzo: Gratz for making it this far, but I’m basically unbeatable.",
        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
        "[BOSS] The Professor: I was burdened with terrible news recently...",
        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
        "[BOSS] Livid: Welcome, you arrive right on time. I am Livid, the Master of Shadows.",
        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!",
        "[BOSS] Maxor: WELL WELL WELL LOOK WHO'S HERE!"
    )

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null) return
        tickCount++
        if (tickCount % 20 != 0) return
        if (config.forceSkyblock) {
            inSkyblock = true
            inDungeons = true
            dungeonFloor = 7
            setFloor()
        } else {
            inSkyblock = onHypixel && mc.theWorld.scoreboard?.getObjectiveInDisplaySlot(1)?.name == "SBScoreboard"

            if (!inDungeons) {
                val line = sidebarLines.find {
                    ScoreboardUtils.cleanSB(it).run { contains("The Catacombs (") && !contains("Queue") }
                } ?: return
                inDungeons = true
                started = System.currentTimeMillis()
                dungeonFloor = line.substringBefore(")").lastOrNull()?.digitToIntOrNull() ?: 0
                masterMode = line.contains(Regex("M\\d\\)"))
                setFloor()
            }
        }
        tickCount = 0
    }

    @SubscribeEvent
    fun onMove(event: LivingEvent.LivingUpdateEvent) {
        if (mc.theWorld == null ||! inDungeons ||! event.entity.equals(mc.thePlayer) || inBoss) return
        setCurrentRoom()
    }

    fun setCurrentRoom() {
        val pos = ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
        val data = ScanUtils.getRoomFromPos(pos)?.data?: return
        val room: Room = Dungeon.Info.uniqueRooms.toList().find { data.name == it.data.name }?: return
        var modified = false
        if (room.direction == null) {
            room.direction = ScanUtils.getDirection(pos.first, pos.second, data, room.core)
            modified = true
        }
        if (room.corner == null && room.direction != null) {
            room.corner = ScanUtils.getCorner(room.direction, room.data.name)
            modified = true
        }
        if (modified) {
            Dungeon.Info.uniqueRooms.remove(room)
            Dungeon.Info.uniqueRooms.add(room)
        }
        if (room != currentRoom || (currentRoom?.direction == null || currentRoom?.corner == null)) {
            currentRoom = room
            Ghostblocks.restored.clear()
            Ghostblocks.render()
            ScoreCalc.calcScore()
        }
    }

    fun setFloor() {
        ScoreCalc.higherFloor = dungeonFloor.equalsOneOf(6, 7)
        ScoreCalc.secretsPercentNeeded = if (masterMode) 1f else 0.2f + min(dungeonFloor, 5) * 0.1f + max(
            dungeonFloor - 5, 0) * 0.15f
    }

    @SubscribeEvent
    fun onChat(event: ChatEvent) {
        if (event.packet.type.toInt() == 2 || !inDungeons) return
        val index = entryMessages.indexOf(event.text) + 1
        if (index != 0) {
            ScoreCalc.calcScore()
            if (dungeonFloor != index) {
                dungeonFloor = index
                ScoreCalc.higherFloor = dungeonFloor.equalsOneOf(6, 7)
            }
            inBoss = true
            Ghostblocks.restored.clear()
            currentRoom = Room(-1, -1, RoomData(index.toString(), RoomType.BOSS, emptyList(), 0, 0, 0, null, null, null, false))
            Ghostblocks.render()
        }
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        onHypixel = mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.lowercase()?.contains("hypixel")) == true)
        }.getOrDefault(false)
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        inDungeons = false
        dungeonFloor = -1
        inBoss = false
        currentRoom = null
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
        inSkyblock = false
        inDungeons = false
        started = 0
        dungeonFloor = -1
        inBoss = false
        currentRoom = null
    }
}
