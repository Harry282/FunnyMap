package funnymap.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import funnymap.FunnyMap.Companion.config
import funnymap.features.dungeon.ScoreCalc
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object APIUtils {
    private var lastCheck = 0L
    fun fetch(uri: String): String {
        HttpClients.createMinimal().use {
            val httpGet = HttpGet(uri)
            return EntityUtils.toString(it.execute(httpGet).entity)
        }
    }

    fun getMayor() {
        if (System.currentTimeMillis() - lastCheck > 150000) {
            lastCheck = System.currentTimeMillis()
            val response = fetch("https://api.hypixel.net/resources/skyblock/election?key=${config.apiKey}")
            val jsonObject = JsonParser().parse(response).toJsonObject() ?: return
            if (jsonObject.getJsonPrimitive("success")?.asBoolean == true) {
                jsonObject.getJsonObject("mayor")?.let { mayor ->
                    if (mayor.get("name")?.asString == "Paul") {
                        mayor.getJsonArray("perks")?.forEach { perk ->
                            if (perk.toJsonObject()?.get("name")?.asString == "EZPZ") {
                                ScoreCalc.isPaul = true
                                return
                            }
                        }
                    }
                    ScoreCalc.isPaul = false
                }
            }
        }
    }

    fun loadPlayerData(uuid: String): JsonObject? {
        val response = fetch("https://api.hypixel.net/player?key=${config.apiKey}&uuid=${uuid}")
        val jsonObject = JsonParser().parse(response).toJsonObject() ?: return null
        return if (jsonObject.getJsonPrimitive("success")?.asBoolean == true) {
            jsonObject
        } else {
            null
        }
    }

    fun getSecrets(playerData: JsonObject) =
        playerData.getJsonObject("player")?.getJsonObject("achievements")?.getJsonPrimitive("skyblock_treasure_hunter")?.asInt ?: 0

    fun getSpirit(playerData: JsonObject, uuid: String): Boolean {
        playerData.getJsonArray("profiles")?.forEach { profile ->
            profile.toJsonObject()?.let { profileData ->
                if (profileData.get("selected")?.asBoolean == true) {
                    profileData.getJsonObject("members")?.getJsonObject(uuid)?.getJsonArray("pets")?.forEach {
                    pet -> pet.toJsonObject()?.let {
                            if (it.get("type")?.asString == "SPIRIT" && it.get("tier")?.asString == "LEGENDARY") return true
                        }
                    }
                }
            }
        }
        return false
    }
}
