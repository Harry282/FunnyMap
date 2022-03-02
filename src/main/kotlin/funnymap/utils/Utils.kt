package funnymap.utils

import com.google.common.collect.ComparisonChain
import funnymap.FunnyMap.Companion.CHAT_PREFIX
import funnymap.FunnyMap.Companion.NEKO_PREFIX
import funnymap.FunnyMap.Companion.mc
import funnymap.FunnyMap.Companion.nekomap
import funnymap.utils.ScoreboardUtils.sidebarLines
import gg.essential.universal.UChat
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.item.ItemStack
import net.minecraft.world.WorldSettings


object Utils {
    fun Any?.equalsOneOf(vararg other: Any): Boolean {
        return other.any {
            this == it
        }
    }

    val currentFloor: Int?
        get() {
            sidebarLines.forEach {
                val line = ScoreboardUtils.cleanSB(it)
                if (line.contains("The Catacombs (")) {
                    val index = line.indexOf(")")
                    if (index != -1) {
                        try {
                            return (line[index - 1]).digitToInt()
                        } catch (_: IllegalArgumentException) {
                        }
                    }

                }
            }
            return null
        }

    fun modMessage(message: String) = UChat.chat("${if (nekomap) NEKO_PREFIX else CHAT_PREFIX} $message")

    val ItemStack.itemID: String
        get() {
            if (this.hasTagCompound() && this.tagCompound.hasKey("ExtraAttributes")) {
                val attributes = this.getSubCompound("ExtraAttributes", false)
                if (attributes.hasKey("id", 8)) {
                    return attributes.getString("id")
                }
            }
            return ""
        }

    private val tabListOrder = Comparator<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@Comparator -1
        if (o2 == null) return@Comparator 0
        return@Comparator ComparisonChain.start().compareTrueFirst(
            o1.gameType != WorldSettings.GameType.SPECTATOR,
            o2.gameType != WorldSettings.GameType.SPECTATOR
        ).compare(
            o1.playerTeam?.registeredName ?: "",
            o2.playerTeam?.registeredName ?: ""
        ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    val tabList: List<Pair<NetworkPlayerInfo, String>>
        get() = (mc.thePlayer?.sendQueue?.playerInfoMap?.sortedWith(tabListOrder) ?: emptyList())
            .map { Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) }


}
