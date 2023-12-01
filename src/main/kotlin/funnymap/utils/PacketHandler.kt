package funnymap.utils

import funnymap.events.ChatEvent
import funnymap.events.ScoreboardEvent
import funnymap.events.TabListEvent
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.common.MinecraftForge

object PacketHandler {
    fun processPacket(packet: Packet<*>) {
        when (packet) {
            is S02PacketChat -> {
                if (packet.type.toInt() != 2) {
                    MinecraftForge.EVENT_BUS.post(ChatEvent(packet))
                }
            }

            is S3EPacketTeams -> {
                MinecraftForge.EVENT_BUS.post(ScoreboardEvent(packet))
            }

            is S38PacketPlayerListItem -> {
                MinecraftForge.EVENT_BUS.post(TabListEvent(packet))
            }
        }
    }
}
