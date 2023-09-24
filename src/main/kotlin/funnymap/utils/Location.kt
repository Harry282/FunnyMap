package funnymap.utils

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.RoomData
import funnymap.core.map.Direction
import funnymap.core.map.Room
import funnymap.core.map.RoomType
import funnymap.events.ChatEvent
import funnymap.features.dungeon.*
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

object Location {

    private var onHypixel = false
    private var inSkyblock = false
    var inDungeons = false
    var started: Long = 0
    var dungeonFloor = -1
    var masterMode = false
    var inBoss = false
    private var lastRoomPos: Pair<Int, Int>? = null
    var currentRoom: Room? = null

    private var tickCount = 0

    private val entryMessages = listOf(
        "[BOSS] Bonzo: Gratz for making it this far, but I'm basically unbeatable.",
        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
        "[BOSS] The Professor: I was burdened with terrible news recently...",
        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
        "[BOSS] Livid: Welcome, you've arrived right on time. I am Livid, the Master of Shadows.",
        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!"
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
        } else if (!inSkyblock) {
            inSkyblock = onHypixel && mc.theWorld.scoreboard?.getObjectiveInDisplaySlot(1)?.name == "SBScoreboard"
        }
        tickCount = 0
    }

    @SubscribeEvent
    fun onMove(event: LivingEvent.LivingUpdateEvent) {
        if (mc.theWorld == null ||! inDungeons ||! event.entity.equals(mc.thePlayer) || inBoss) return
        ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt()).run {
            if (this != lastRoomPos) {
                lastRoomPos = this
                setCurrentRoom(this)
            }
        }
    }

    fun setCurrentRoom(pos: Pair<Int, Int>) {
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
            ScoreCalc.calcScore()
        }
    }

    fun relativeOfActual(pos: BlockPos) : BlockPos? {
        if (currentRoom == null || currentRoom?.data?.type == RoomType.BOSS) return BlockPos(pos.x, pos.y, pos.z)
        val corner = currentRoom?.corner ?: return null
        return when (currentRoom?.direction) {
            Direction.NW -> BlockPos(pos.x - corner.x, pos.y, pos.z - corner.y)
            Direction.NE -> BlockPos(pos.z - corner.y, pos.y, -(pos.x - corner.x))
            Direction.SE -> BlockPos(-(pos.x - corner.x), pos.y, -(pos.z - corner.y))
            Direction.SW -> BlockPos(-(pos.z - corner.y), pos.y, pos.x - corner.x)
            else -> null
        }
    }

    fun actualOfRelative(pos: BlockPos) : BlockPos? {
        if (currentRoom == null || currentRoom?.data?.type == RoomType.BOSS) return pos
        val corner = currentRoom?.corner ?: return null
        return when (currentRoom?.direction) {
            Direction.NW -> BlockPos(pos.x + corner.x, pos.y, pos.z + corner.y)
            Direction.NE -> BlockPos(-(pos.z - corner.x), pos.y, pos.x + corner.y)
            Direction.SE -> BlockPos(-(pos.x - corner.x), pos.y, -(pos.z - corner.y))
            Direction.SW -> BlockPos(pos.z + corner.x, pos.y, -(pos.x - corner.y))
            else -> null
        }
    }


    @SubscribeEvent(receiveCanceled = true)
    fun onChat(event: ChatEvent) {
        if (event.packet.type.toInt() == 2 || !inDungeons || inBoss) return
        val index = (entryMessages.indexOf(event.text) + 1).let { if (it == 0) { if (event.text.startsWith("[BOSS] Maxor: ")) 7 else 0 } else it }
        if (index != 0) {
            dungeonFloor = index
            inBoss = true
            currentRoom = Room(-1, -1, RoomData(dungeonFloor.toString(), RoomType.BOSS, emptyList(), 0, 0, 0, null, null, null, false))
            ScoreCalc.calcScore()
        }
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        onHypixel = mc.runCatching {
            !event.isLocal && ((thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.lowercase()?.contains("hypixel")) == true)
        }.getOrDefault(false)
        if (onHypixel) {
            APIUtils.getMayor()
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        inDungeons = false
        dungeonFloor = -1
        inBoss = false
        lastRoomPos = null
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
