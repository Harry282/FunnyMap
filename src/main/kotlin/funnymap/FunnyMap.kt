package funnymap

import funnymap.commands.FunnyMapCommands
import funnymap.config.Config
import funnymap.features.dungeon.Dungeon
import funnymap.features.dungeon.MapRender
import funnymap.utils.ScoreboardUtils
import funnymap.utils.UpdateChecker
import gg.essential.api.EssentialAPI
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import java.awt.Desktop
import java.io.File
import java.net.URI

@Mod(
    modid = FunnyMap.MOD_ID,
    name = FunnyMap.MOD_NAME,
    version = FunnyMap.MOD_VERSION
)
class FunnyMap {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val directory = File(event.modConfigurationDirectory, "funnymap")
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        ClientCommandHandler.instance.registerCommand((FunnyMapCommands()))
        listOf(
            this,
            Dungeon,
            MapRender
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun postInit(event: FMLLoadCompleteEvent) = runBlocking {
        launch {
            if (UpdateChecker.hasUpdate() > 0) {
                try {
                    EssentialAPI.getNotifications().push(
                        MOD_NAME,
                        "New release available on Github. Click to open download link.",
                        10f,
                        action = {
                            Desktop.getDesktop().browse(URI("https://github.com/Harry282/FunnyMap/releases"))
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickCount++
        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }
        if (tickCount % 20 == 0) {
            if (mc.thePlayer != null) {
                val onHypixel = EssentialAPI.getMinecraftUtil().isHypixel()

                inSkyblock = config.forceSkyblock || onHypixel && mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
                    ?.let { ScoreboardUtils.cleanSB(it.displayName).contains("SKYBLOCK") } ?: false

                inDungeons = config.forceSkyblock || inSkyblock && ScoreboardUtils.sidebarLines.any {
                    ScoreboardUtils.cleanSB(it).run {
                        (contains("The Catacombs") && !contains("Queue")) || contains("Dungeon Cleared:")
                    }
                }
            }
            tickCount = 0
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        inSkyblock = false
        inDungeons = false
    }

    companion object {
        const val MOD_ID = "funnymap"
        const val MOD_NAME = "Funny Map"
        const val MOD_VERSION = "0.5.0"
        const val CHAT_PREFIX = "§b§l<§fFunny Map§b§l>§r"
        const val NEKO_PREFIX = "§b§l<§dNeko §fMap§b§l>§r"

        val mc: Minecraft = Minecraft.getMinecraft()
        val config = Config
        var display: GuiScreen? = null
        var tickCount = 0

        var nekomap = File(mc.mcDataDir, "config/funnymap/nekomap").exists()

        var inSkyblock = false
        var inDungeons = false
    }
}
