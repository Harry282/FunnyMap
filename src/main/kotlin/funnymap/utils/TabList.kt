package funnymap.utils

import com.google.common.collect.ComparisonChain
import funnymap.features.dungeon.MapUpdate
import funnymap.features.dungeon.RunInformation
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.world.WorldSettings.GameType
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList

object TabList {
    val TabList = TreeSet(TabListComparator())
    private val playerInfoClass = NetworkPlayerInfo::class.java
    private val setGameTypeMethod: Method = playerInfoClass.getDeclaredMethod("func_178839_a", GameType::class.java).apply { isAccessible = true }
    //private val setResponseTimeMethod: Method = playerInfoClass.getDeclaredMethod("func_178838_a", Int::class.java).apply { isAccessible = true }

    class TabListComparator : Comparator<NetworkPlayerInfo> {
        override fun compare(o1: NetworkPlayerInfo?, o2: NetworkPlayerInfo?): Int {
            if (o1?.gameProfile?.name == null) return -1
            else if (o2?.gameProfile?.name == null) return 0

            return ComparisonChain.start().compareTrueFirst(
                o1.gameType != GameType.SPECTATOR, o2.gameType != GameType.SPECTATOR
            ).compare(
                o1.safePlayerTeam()?.registeredName ?: "", o2.safePlayerTeam()?.registeredName ?: ""
            ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
        }
    }

    fun handlePacket(packet: S38PacketPlayerListItem) {
        val updated: ArrayList<NetworkPlayerInfo> = arrayListOf()
        packet.entries.forEach { player ->
            if (player == null) return@forEach
            if (packet.action == S38PacketPlayerListItem.Action.REMOVE_PLAYER) {
                TabList.removeIf { it.gameProfile.id == player.profile.id }
            } else {
                var networkPlayerInfo = TabList.find { it.gameProfile.id == player.profile.id }
                if (packet.action == S38PacketPlayerListItem.Action.ADD_PLAYER) {
                    networkPlayerInfo = NetworkPlayerInfo(player)
                    TabList.removeIf { it.gameProfile.id == player.profile.id }
                    TabList.add(networkPlayerInfo)
                }
                if (networkPlayerInfo != null) {
                    when (packet.action) {
                        S38PacketPlayerListItem.Action.ADD_PLAYER -> setGameTypeMethod.invoke(networkPlayerInfo, player.gameMode)

                        S38PacketPlayerListItem.Action.UPDATE_GAME_MODE -> setGameTypeMethod.invoke(networkPlayerInfo, player.gameMode)

                        S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME -> networkPlayerInfo.displayName = player.displayName

                        else -> return@forEach
                    }
                    updated.add(networkPlayerInfo)
                }
            }
        }
        if (Location.inDungeons && checkTabList()) {
            if (updated.isNotEmpty()) {
                updated.forEach {
                    RunInformation.updateRunInformation(it)
                }
                MapUpdate.updatePlayer(TabList.toMutableList())
            }
        }
    }

    fun checkTabList() = TabList.size > 18 && TabList.first()?.displayName?.formattedText?.contains("§r§b§lParty §r§f(") == true

    fun NetworkPlayerInfo.safePlayerTeam() = funnymap.FunnyMap.mc.theWorld?.scoreboard?.getPlayersTeam(this.gameProfile?.name)
}
