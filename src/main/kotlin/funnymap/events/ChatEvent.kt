package funnymap.events

import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.Event

class ChatEvent(val packet: S02PacketChat) : Event()
