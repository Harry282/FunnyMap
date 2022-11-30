package funnymap.utils

import com.google.gson.JsonParser
import funnymap.FunnyMap.Companion.config
import funnymap.events.ChatEvent
import funnymap.features.dungeon.PlayerTracker
import funnymap.utils.LocationUtils.inDungeons
import net.minecraft.event.ClickEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object APIUtils {
    @SubscribeEvent
    fun onChatPacket(event: ChatEvent) {
        if (event.packet.type.toInt() == 2) return
        if (event.text.startsWith("Your new API key is ") && event.packet.chatComponent.siblings.size >= 1) {
            val apiKey = event.packet.chatComponent.siblings[0].chatStyle.chatClickEvent.value
            config.apiKey = apiKey
            config.markDirty()
            Utils.modMessage("Updated API key to $apiKey.")
            return
        }
        if (!inDungeons || !config.teamInfo) return
        if (event.packet.chatComponent.siblings.any {
                it.chatStyle?.chatClickEvent?.run {
                    action == ClickEvent.Action.RUN_COMMAND && value == "/showextrastats"
                } == true
            }) {
            PlayerTracker.onDungeonEnd()
        }
    }

    fun fetch(uri: String): String {
        HttpClients.createMinimal().use {
            val httpGet = HttpGet(uri)
            return EntityUtils.toString(it.execute(httpGet).entity)
        }
    }

    fun getSecrets(uuid: String): Int {
        val response = fetch("https://api.hypixel.net/player?key=${config.apiKey}&uuid=${uuid}")
        val jsonObject = JsonParser().parse(response).asJsonObjectOrNull() ?: return 0
        if (jsonObject.getAsJsonPrimitiveOrNull("success")?.asBoolean == true) {
            return jsonObject.getAsJsonObjectOrNull("player")?.getAsJsonObjectOrNull("achievements")
                ?.getAsJsonPrimitiveOrNull("skyblock_treasure_hunter")?.asInt ?: return 0
        }
        return 0
    }
}
