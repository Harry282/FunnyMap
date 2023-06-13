package funnymap.commands

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.display
import funnymap.FunnyMap.Companion.mc
import funnymap.core.map.Direction
import funnymap.features.dungeon.*
import funnymap.utils.LocationUtils
import funnymap.utils.Utils
import gg.essential.universal.UChat
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import java.io.File

class FunnyMapCommands : CommandBase() {

    private val commands = listOf("help", "scan", "roomdata")

    override fun getCommandName(): String = "funnymap"

    override fun getCommandAliases(): List<String> = listOf("fmap", "fm")

    override fun getCommandUsage(sender: ICommandSender): String = "/$commandName"

    override fun getRequiredPermissionLevel(): Int = 0

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            display = config.gui()
            return
        }
        when (args[0]) {
            // Help command
            "help" -> {
                UChat.chat(
                    """
                        #§b§l<§fFunnyMap Commands§b§l>
                        #  §b/funnymap §9> §3Opens the main mod GUI. §7(Alias: fm, fmap)
                        #  §b/§ffunnymap §bscan §9> §3Rescans the map.
                        #  §b/§ffunnymap §broomdata §9> §3Copies current room data or room core to clipboard.
                    """.trimMargin("#")
                )
            }
            // Scans the dungeon
            "scan" -> {
                Dungeon.reset()
                DungeonScan.scan()
            }
            // Copies room data or room core to clipboard
            "roomdata" -> {
                val pos = ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
                val data = ScanUtils.getRoomData(pos.first, pos.second)
                if (data != null) {
                    GuiScreen.setClipboardString(data.toString())
                    Utils.modMessage("Copied room data to clipboard.")
                } else {
                    GuiScreen.setClipboardString(ScanUtils.getCore(pos.first, pos.second).toString())
                    Utils.modMessage("Existing room data not found. Copied room core to clipboard.")
                }
            }
            "getdirection" -> {
                var distance = 4
                if (args.size >= 2) {
                    try {
                        distance = args[1].toInt()
                    } catch (_: NumberFormatException) {

                    }
                }
                var cores = ""
                val pos = ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
                Direction.values().forEach { cores += " " + ScanUtils.getPosCore(it, pos.first, pos.second, distance) }
                Utils.modMessage("Cores:$cores")
            }
            "runstats" -> {
                Utils.modMessage("Master: ${LocationUtils.masterMode} ${LocationUtils.dungeonFloor} First death spirit: ${ScoreCalc.firstDeathHadSpirit} Paul: ${ScoreCalc.isPaul} Rooms: ${Dungeon.Info.rooms}\n" +
                        "Secrets ${100f * RunInformation.secretsFound / Dungeon.Info.secretCount} Needed: ${ScoreCalc.secretsPercentNeeded * 100f}\n" +
                        "Skill ${ScoreCalc.skillScore} Exploration ${ScoreCalc.explorationScore} Speed ${ScoreCalc.speedScore} Bonus ${ScoreCalc.bonusScore}")
            }
            "delete" -> {
                if (Ghostblocks.deleteStatus != null) {
                    when (args[1]) {
                        "yes" -> {
                            var success = false
                            val name = Ghostblocks.getName(Ghostblocks.deleteStatus?.data?.name?: "AAB")
                            if (name != "AAB") {
                                if (Ghostblocks.blocks[Ghostblocks.deleteStatus?.data?.type.toString()]?.remove(name) != null) {
                                    if (File("${Ghostblocks.dir}${Ghostblocks.deleteStatus?.data?.type}/${name}.json").delete()) {
                                        success = true
                                        if (LocationUtils.currentRoom?.data?.name == (Ghostblocks.deleteStatus?.data?.name ?: "AAB")) Ghostblocks.reload()
                                    }
                                }
                            }
                            if (success) Utils.modMessage("Deleted all ghostblocks in ${Ghostblocks.deleteStatus?.data?.name}")
                            else Utils.modMessage("§cFailed to delete")
                            Ghostblocks.deleteStatus = null
                        }
                        "no" -> {
                            Ghostblocks.deleteStatus = null
                            Utils.modMessage("Cancelled deletion")
                        }
                    }
                }
            }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<String>,
        pos: BlockPos,
    ): MutableList<String> {
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, commands)
        }
        return super.addTabCompletionOptions(sender, args, pos)
    }
}
