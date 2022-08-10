package funnymap.commands

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.display
import funnymap.FunnyMap.Companion.mc
import funnymap.core.RoomData
import funnymap.features.dungeon.DungeonScan
import funnymap.features.dungeon.ScanUtils
import funnymap.utils.Utils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class FunnyMapCommands : CommandBase() {
    override fun getCommandName(): String = "funnymap"

    override fun getCommandAliases(): List<String> = listOf("fmap")

    override fun getCommandUsage(sender: ICommandSender): String = "/$commandName"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            display = config.gui()
            return
        }
        when (args[0]) {
            "scan" -> DungeonScan.scanDungeon()
            "roomdata" -> ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt()).let {
                ScanUtils.getRoomData(it.first, it.second) ?: ScanUtils.getCore(it.first, it.second)
            }.run {
                GuiScreen.setClipboardString(this.toString())
                Utils.modMessage(
                    if (this is RoomData) "Copied room data to clipboard."
                    else "Existing room data not found. Copied room core to clipboard."
                )
            }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos,
    ): MutableList<String> {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, mutableListOf("scan", "roomdata"))
        }
        return super.addTabCompletionOptions(sender, args, pos)
    }
}
