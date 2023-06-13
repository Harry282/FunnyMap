package funnymap.utils

import com.google.common.collect.ComparisonChain
import funnymap.FunnyMap.Companion.CHAT_PREFIX
import funnymap.FunnyMap.Companion.mc
import gg.essential.universal.UChat
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.item.ItemStack
import net.minecraft.world.WorldSettings


object Utils {
    fun Any?.equalsOneOf(vararg other: Any): Boolean = other.any { this == it }

    fun modMessage(message: String) = UChat.chat("$CHAT_PREFIX $message")

    fun showClientTitle(title: String, subtitle: String) {
        mc.ingameGUI.displayTitle(null, null, 2, 40, 2)
        mc.ingameGUI.displayTitle(null, subtitle, -1, -1, -1)
        mc.ingameGUI.displayTitle(title, null, -1, -1, -1)
    }

    val ItemStack.itemID: String
        get() = this.getSubCompound("ExtraAttributes", false)?.getString("id") ?: ""

    private val tabListOrder = Comparator<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@Comparator -1
        if (o2 == null) return@Comparator 0
        return@Comparator ComparisonChain.start().compareTrueFirst(
            o1.gameType != WorldSettings.GameType.SPECTATOR, o2.gameType != WorldSettings.GameType.SPECTATOR
        ).compare(
            o1.playerTeam?.registeredName ?: "", o2.playerTeam?.registeredName ?: ""
        ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    val tabList: List<Pair<NetworkPlayerInfo, String>>
        get() = mc.thePlayer?.sendQueue?.playerInfoMap?.sortedWith(tabListOrder)?.map {
            Pair(it, mc.ingameGUI.tabList.getPlayerName(it))
        } ?: emptyList()

    val dungeonTabList: List<Pair<NetworkPlayerInfo, String>>?
        get() {
            val tabEntries = tabList
            return if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) null else tabEntries
        }
}
