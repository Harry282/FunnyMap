package funnymap.utils

import com.google.gson.JsonParser
import funnymap.FunnyMap.Companion.config
import funnymap.events.ChatEvent
import funnymap.features.dungeon.PlayerTracker
import funnymap.features.dungeon.ScoreCalc
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

    fun getMayor() {
        val response = fetch("https://api.hypixel.net/resources/skyblock/election?key=${config.apiKey}")
        val jsonObject = JsonParser().parse(response).asJsonObjectOrNull() ?: return
        if (jsonObject.getAsJsonPrimitiveOrNull("success")?.asBoolean == true) {
            jsonObject.getAsJsonObjectOrNull("mayor")?.let { mayor ->
                if (mayor.get("name")?.asString == "Paul") {
                    mayor.getAsJsonArray("perks")?.forEach { perk ->
                        if (perk.asJsonObjectOrNull()?.get("name")?.asString == "EZPZ") {
                            ScoreCalc.isPaul = true
                            return
                        }
                    }
                }
            }
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

    fun getSpirit(id: String): Boolean {
        val uuid = id.replace("-", "")
        val response = fetch("https://api.hypixel.net/skyblock/profiles?key=${config.apiKey}&uuid=$uuid")
        val jsonObject = JsonParser().parse(response).asJsonObjectOrNull() ?: return false
        if (jsonObject.getAsJsonPrimitiveOrNull("success")?.asBoolean == true) {
            jsonObject.getAsJsonArray("profiles")?.forEach { profile ->
                profile.asJsonObjectOrNull()?.let {profileData ->
                    if (profileData.get("selected")?.asBoolean == true) {
                        profileData.getAsJsonObjectOrNull("members")?.getAsJsonObjectOrNull(uuid)?.getAsJsonArray("pets")?.forEach {
                            pet -> pet.asJsonObjectOrNull()?.let {
                                if (it.get("type")?.asString == "SPIRIT" && it.get("tier")?.asString == "LEGENDARY") return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}
