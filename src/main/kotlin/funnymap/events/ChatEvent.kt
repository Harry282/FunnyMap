package funnymap.events

import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.common.eventhandler.Event

class ChatEvent(val packet: S02PacketChat) : Event() {
    val text: String by lazy { StringUtils.stripControlCodes(packet.chatComponent.unformattedText) }
    val formatted: String = packet.chatComponent.formattedText
}
