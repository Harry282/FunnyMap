package funnymap.utils

import funnymap.events.ChatEvent
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import net.minecraftforge.common.MinecraftForge

object PacketHandler {
    fun processPacket(packet: Packet<*>) {
        when (packet) {
            is S02PacketChat -> {
                MinecraftForge.EVENT_BUS.post(ChatEvent(packet))
            }
            is S3EPacketTeams -> {
                Scoreboard.onScoreboard(packet)
            }
            is S38PacketPlayerListItem -> {
                TabList.handlePacket(packet)
            }
        }
    }
}
