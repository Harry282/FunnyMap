package funnymap

import funnymap.commands.FunnyMapCommands
import funnymap.config.Config
import funnymap.events.PacketHandler
import funnymap.features.dungeon.Dungeon
import funnymap.features.dungeon.Ghostblocks
import funnymap.features.dungeon.MapRender
import funnymap.features.dungeon.ScoreCalc
import funnymap.utils.APIUtils
import funnymap.utils.LocationUtils
import funnymap.utils.UpdateChecker
import gg.essential.api.EssentialAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
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
            this, APIUtils, Dungeon, Ghostblocks, LocationUtils, MapRender, PacketHandler, ScoreCalc
        ).forEach(MinecraftForge.EVENT_BUS::register)
        keybindings.forEach { ClientRegistry.registerKeyBinding(it) }
        Ghostblocks.loadData()
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
        if (event.phase != TickEvent.Phase.START || display == null) return
        mc.displayGuiScreen(display)
        display = null
    }

    companion object {
        const val MOD_ID = "funnymap"
        const val MOD_NAME = "Funny Map"
        const val MOD_VERSION = "0.7.0"
        const val CHAT_PREFIX = "§b§l<§fFunny Map§b§l>§r"

        val mc: Minecraft = Minecraft.getMinecraft()
        val config = Config
        var display: GuiScreen? = null
        val scope = CoroutineScope(EmptyCoroutineContext)
        val keybindings: Array<KeyBinding> = arrayOf(
            KeyBinding("Create GBs", Keyboard.KEY_G, "FunnyMap"),
            KeyBinding("Place blocks", Keyboard.KEY_F, "FunnyMap"),
            KeyBinding("Delete GBs in radius", Keyboard.KEY_J, "FunnyMap"),
            KeyBinding("Reload data", Keyboard.KEY_H, "FunnyMap"),
            KeyBinding("Unload data", Keyboard.KEY_NONE, "FunnyMap"))
    }
}
