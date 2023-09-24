package funnymap.features.dungeon

import funnymap.FunnyMap
import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.FunnyMap.Companion.scope
import funnymap.core.DungeonPlayer
import funnymap.core.RoomData
import funnymap.core.map.Room
import funnymap.core.map.RoomState
import funnymap.core.map.RoomType
import funnymap.core.map.Tile
import funnymap.utils.APIUtils
import funnymap.utils.Location
import funnymap.utils.Utils.equalsOneOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.StringUtils
import kotlin.time.Duration.Companion.milliseconds

object PlayerTracker {

    val roomClears: MutableMap<RoomData, Set<String>> = mutableMapOf()

    fun roomStateChange(room: Tile, state: RoomState, newState: RoomState) {
        if (room !is Room) return
        if (newState.equalsOneOf(RoomState.CLEARED, RoomState.GREEN) && state != RoomState.CLEARED) {
            val currentRooms =
                Dungeon.dungeonTeammates.map { Pair(it.value.formattedName, it.value.getCurrentRoom()) }
            roomClears[room.data] =
                currentRooms.filter { it.first != "" && it.second == room.data.name }.map { it.first }.toSet()
        }
    }


    fun onTerminalPhaseEnd() {
        Dungeon.dungeonTeammates.forEach {
            sendTerminalsMessage(it.value.formattedName, it.value)
        }
    }

    fun onDungeonEnd() {
        val time = System.currentTimeMillis() - Dungeon.Info.startTime
        Dungeon.dungeonTeammates.forEach {
            it.value.roomVisits.add(Pair(time - it.value.lastTime, it.value.lastRoom))
        }

        scope.launch {
            Dungeon.dungeonTeammates.map { (_, player) ->
                async(Dispatchers.IO) {
                    Triple(
                        player.formattedName,
                        player,
                        APIUtils.loadPlayerData(player.uuid)?.let { APIUtils.getSecrets(it) } ?: 0
                    )
                }
            }.forEach {
                val (name, player, secrets) = it.await()
                sendStatMessage(name, player, secrets)
            }
        }
    }

    private fun sendStatMessage(name: String, player: DungeonPlayer, secrets: Int) {
        val secretsComponent = ChatComponentText("§b${secrets - player.startingSecrets} §3secrets")

        val allClearedRooms = roomClears.filter { it.value.contains(name) }
        val soloClearedRooms = allClearedRooms.filter { it.value.size == 1 }
        val max = allClearedRooms.size
        val min = soloClearedRooms.size

        val roomComponent = ChatComponentText(
            "§b${if (soloClearedRooms.size != allClearedRooms.size) "$min-$max" else max} §3rooms cleared"
        ).apply {
            chatStyle =
                ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                    roomClears.entries.filter {
                        !it.key.type.equalsOneOf(RoomType.BLOOD, RoomType.ENTRANCE, RoomType.FAIRY) &&
                                it.value.contains(name)
                    }.joinToString(
                        separator = "\n",
                        prefix = "$name's §eRooms Cleared:\n"
                    ) { (room, players) ->
                        if (players.size == 1) {
                            "§6${room.name}"
                        } else {
                            "§6${room.name} §7with ${players.filter { it != name }.joinToString(separator = "§r, ")}"
                        }
                    }
                )))
        }

        var lastTime = 0L

        val splitsComponent = ChatComponentText("§3Room Splits").apply {
            chatStyle = ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                player.roomVisits.joinToString(
                    separator = "\n",
                    prefix = "$name's §eRoom Splits:\n"
                ) { (elapsed, room) ->
                    val start = lastTime.milliseconds
                    lastTime += elapsed
                    val end = lastTime.milliseconds
                    "§b$start §7- §b$end §6$room"
                }
            )))
        }

        val roomTimeComponent = ChatComponentText("§3Room Times").apply {
            chatStyle = ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                player.roomVisits.groupBy { it.second }.entries.joinToString(
                    separator = "\n",
                    prefix = "$name's §eRoom Times:\n"
                ) { (room, times) ->
                    "§6$room §a- §b${times.sumOf { it.first }.milliseconds}"
                }
            )))
        }

        val terminalComponent =
            if (config.teamInfoTerminals && Location.dungeonFloor == 7) ChatComponentText("§3Terminals: ${calculateTerminalRating(player)}").apply {
                chatStyle = ChatStyle().setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                    """$name's §eTerminals:
                        #§6Devices §b${player.devices}
                        #§6Terminals §b${player.terminals}
                        #§6Levers §b${player.levers}""".trimMargin("#")
                )))
            }
            else ChatComponentText("")

        val nameComponent = ChatComponentText(name).apply {
            chatStyle = ChatStyle().setChatHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                        "Click to paste in chat"
                    )
                )
            ).setChatClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, StringUtils.stripControlCodes(ChatComponentText("$name > ").appendSibling(secretsComponent)
                .appendText(" | ").appendSibling(roomComponent).appendText(" | ").appendSibling(terminalComponent).unformattedText)))
        }

        mc.thePlayer.addChatMessage(
            ChatComponentText("${FunnyMap.CHAT_PREFIX} §3").appendSibling(nameComponent).appendText(" §f> ")
                .appendSibling(secretsComponent).appendText(" §6| ")
                    .appendSibling(roomComponent).appendText(" §6| ")
                    .appendSibling(splitsComponent).appendText(" §6| ")
                    .appendSibling(roomTimeComponent).appendText(" §6| ")
                    .appendSibling(terminalComponent)
        )
    }

    private fun sendTerminalsMessage(name: String, player: DungeonPlayer) {
        val terminals = ChatComponentText("§b${player.devices} §3Devices §6| §b${player.terminals} §3Terminals §6| §b${player.levers} §3Levers")

        val nameComponent = ChatComponentText(name).apply {
            chatStyle = ChatStyle().setChatHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT, ChatComponentText(
                        "Click to paste in chat"
                    )
                )
            ).setChatClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, StringUtils.stripControlCodes("$name > ${terminals.unformattedText}")))
        }

        mc.thePlayer.addChatMessage(
            ChatComponentText("${FunnyMap.CHAT_PREFIX} §3").appendSibling(nameComponent).appendText(" §f> ")
                .appendSibling(terminals)
        )
    }

    private fun calculateTerminalRating(player: DungeonPlayer) : String {
        val score = (player.devices * config.deviceValue + player.terminals * config.terminalValue + player.levers * config.leverValue) * Dungeon.dungeonTeammates.size / 5f
        return when {
            score <= config.atrociousThreshold -> "§8Atrocious§4!"
            score <= config.badThreshold -> "§cBad"
            score <= config.alrightThreshold -> "§eAlright"
            score <= config.goodThreshold -> "§aGood"
            else -> "§6Excellent"
        }
    }
}