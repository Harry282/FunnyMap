package funnymap.utils

import funnymap.features.dungeon.MapUpdate
import funnymap.features.dungeon.RunInformation
import funnymap.utils.Location.dungeonFloor
import funnymap.utils.Location.inDungeons
import funnymap.utils.Location.masterMode
import funnymap.utils.Location.started
import funnymap.utils.TabList.TabList
import funnymap.utils.TabList.checkTabList
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.util.StringUtils

object Scoreboard {
    var tries = 0
    fun onScoreboard(packet: S3EPacketTeams) {
        if (packet.action != 2 || inDungeons || tries > 75) return
        tries++
        StringUtils.stripControlCodes(packet.players.joinToString(
            " ",
            prefix = packet.prefix,
            postfix = packet.suffix
        ))?.run {
            if (contains("The Catacombs (") && !contains("Queue")) {
                inDungeons = true
                started = System.currentTimeMillis()
                dungeonFloor = this.substringBefore(")").lastOrNull()?.digitToIntOrNull() ?: 0
                masterMode = this[this.length - 3] == 'M'
                TabList.takeIf { checkTabList() }?.apply {
                    forEach { RunInformation.updateRunInformation(it) }
                    MapUpdate.updatePlayer(toMutableList())
                }
            }
        }
    }
}
