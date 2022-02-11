package funnymap.commands

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.display
import funnymap.features.dungeon.Dungeon
import funnymap.features.dungeon.Dungeon.Companion.dungeonList
import funnymap.features.dungeon.DungeonScan
import funnymap.features.dungeon.MapUpdate
import funnymap.utils.MapUtils
import funnymap.utils.ScoreboardUtils
import funnymap.utils.Utils
import gg.essential.universal.UChat
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class FunnyMapCommands : CommandBase() {
    override fun getCommandName(): String {
        return "funnymap"
    }

    override fun getCommandAliases(): List<String> {
        return listOf(
            "fmap"
        )
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/$commandName"
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            display = config.gui()
            return
        }
        when (args[0]) {
            "scan" -> DungeonScan.scanDungeon()
            "array" -> {
                for (i in 0..10) {
                    UChat.chat(
                        dungeonList.slice(0 + 11 * i until 11 + 11 * i).joinToString(
                            prefix = "[",
                            postfix = "]"
                        )
                    )
                }
            }
            "roomarray" -> {
                for (i in 0..10 step 2) {
                    UChat.chat(
                        dungeonList.slice(0 + 11 * i until 11 + 11 * i step 2).joinToString(
                            prefix = "[",
                            postfix = "]"
                        )
                    )
                }
            }
            "tablist" -> {
                UChat.chat(Utils.tabList.joinToString(separator = "\n") { ScoreboardUtils.cleanSB(it.second) })
            }
            "teammates" -> {
                MapUpdate.getPlayers()
                UChat.chat(Dungeon.dungeonTeamates.joinToString { it.name })
            }
            "setmap" -> {
                if (args.size == 3) {
                    try {
                        val x = args[1].toInt()
                        val z = args[2].toInt()
                        MapUtils.startCorner = Pair(x, z)
                    } catch (_: NumberFormatException) {
                    }
                }
            }
        }
    }
}
