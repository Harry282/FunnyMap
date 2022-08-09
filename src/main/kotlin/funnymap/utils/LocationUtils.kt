package funnymap.utils

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
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

    init {
        fixedRateTimer(period = 1000) {
            if (mc.theWorld != null) {
                if (config.forceSkyblock) {
                    inSkyblock = true
                    inDungeons = true
                    dungeonFloor = 7
                } else {
                    inSkyblock = onHypixel && mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
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
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        onHypixel = false
        inSkyblock = false
        inDungeons = false
        dungeonFloor = -1
    }
}
