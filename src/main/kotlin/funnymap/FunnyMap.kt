package funnymap

import funnymap.commands.FunnyMapCommands
import funnymap.config.Config
import funnymap.features.dungeon.Dungeon
import funnymap.features.dungeon.RunInformation
import funnymap.features.dungeon.WitherDoorESP
import funnymap.ui.GuiRenderer
import funnymap.utils.Location
import funnymap.utils.UpdateChecker
import gg.essential.api.EssentialAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.coroutines.EmptyCoroutineContext

@Mod(
    modid = FunnyMap.MOD_ID, name = FunnyMap.MOD_NAME, version = FunnyMap.MOD_VERSION
)
class FunnyMap {
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        File(event.modConfigurationDirectory, "funnymap").mkdirs()
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        ClientCommandHandler.instance.registerCommand((FunnyMapCommands()))
        listOf(
            this, Dungeon, GuiRenderer, Location, RunInformation, WitherDoorESP
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun postInit(event: FMLLoadCompleteEvent) = scope.launch {
        if (UpdateChecker.hasUpdate() > 0) {
            EssentialAPI.getNotifications()
                .push(MOD_NAME, "New release available on Github. Click to open download link.", 10f, action = {
                    try {
                        Desktop.getDesktop().browse(URI("https://github.com/Harry282/FunnyMap/releases"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                })
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        mc.mcProfiler.startSection("funnymap")

        if (display != null) {
            mc.displayGuiScreen(display)
            display = null
        }

        Dungeon.onTick()
        GuiRenderer.onTick()
        Location.onTick()

        mc.mcProfiler.endSection()
    }

    companion object {
        const val MOD_ID = "funnymap"
        const val MOD_NAME = "Funny Map"
        const val MOD_VERSION = "0.7.4"
        const val CHAT_PREFIX = "§b§l<§fFunny Map§b§l>§r"

        val mc: Minecraft = Minecraft.getMinecraft()
        val config = Config
        var display: GuiScreen? = null
        val scope = CoroutineScope(EmptyCoroutineContext)
    }
}
