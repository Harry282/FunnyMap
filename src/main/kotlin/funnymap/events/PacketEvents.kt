package funnymap.events

import funnymap.utils.Utils.removeFormatting
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.Event

class ChatEvent(val packet: S02PacketChat) : Event() {
    val text: String by lazy { packet.chatComponent.unformattedText.removeFormatting() }
    val formatted: String = packet.chatComponent.formattedText
}
