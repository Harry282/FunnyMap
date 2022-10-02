package funnymap.utils

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.events.ChatEvent
import funnymap.utils.ScoreboardUtils.sidebarLines
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import kotlin.concurrent.fixedRateTimer

object LocationUtils {
    private var onHypixel = false
    var inSkyblock = false
    var inDungeons = false
    var dungeonFloor = -1
    var inBoss = false

    private val entryMessages = listOf(
        "[BOSS] Bonzo: Gratz for making it this far, but I’m basically unbeatable.",
        "[BOSS] Scarf: This is where the journey ends for you, Adventurers.",
        "[BOSS] The Professor: I was burdened with terrible news recently...",
        "[BOSS] Thorn: Welcome Adventurers! I am Thorn, the Spirit! And host of the Vegan Trials!",
        "[BOSS] Livid: Welcome, you arrive right on time. I am Livid, the Master of Shadows.",
        "[BOSS] Sadan: So you made it all the way here... Now you wish to defy me? Sadan?!",
        "[BOSS] Maxor: WELL WELL WELL LOOK WHO’S HERE!"
    )

    init {
        fixedRateTimer(period = 1000) {
            if (mc.theWorld != null) {
                if (config.forceSkyblock) {
                    inSkyblock = true
                    inDungeons = true
                    dungeonFloor = 7
                } else {
                    inSkyblock = onHypixel && mc.theWorld.scoreboard?.getObjectiveInDisplaySlot(1)
                        ?.let { ScoreboardUtils.cleanSB(it.displayName).contains("SKYBLOCK") } ?: false

                    if (!inDungeons) {
                        val line = sidebarLines.find {
                            ScoreboardUtils.cleanSB(it).run { contains("The Catacombs (") && !contains("Queue") }
                        } ?: return@fixedRateTimer
                        inDungeons = true
                        dungeonFloor = line.substringBefore(")").lastOrNull()?.digitToIntOrNull() ?: 0
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatEvent) {
        if (event.packet.type.toInt() == 2 || !inDungeons) return
        if (entryMessages.any { it == event.text }) inBoss = true
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
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
        inSkyblock = false
        inDungeons = false
        dungeonFloor = -1
        inBoss = false
    }
}
