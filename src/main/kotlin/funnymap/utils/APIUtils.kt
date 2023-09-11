package funnymap.utils

import com.google.gson.JsonParser
import funnymap.FunnyMap.Companion.config
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object APIUtils {
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
