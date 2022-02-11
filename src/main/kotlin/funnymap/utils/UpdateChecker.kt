package funnymap.utils

import com.google.gson.JsonParser
import funnymap.FunnyMap
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object UpdateChecker {
    fun hasUpdate(): Int {
        HttpClients.createMinimal().use {
            val httpGet = HttpGet("https://api.github.com/repos/Harry282/FunnyMap/releases")
            val response = EntityUtils.toString(it.execute(httpGet).entity)
            val version = JsonParser().parse(response).asJsonArray[0].asJsonObject["tag_name"]
            val current = DefaultArtifactVersion(FunnyMap.MOD_VERSION.replace("pre", "beta"))
            val latest = DefaultArtifactVersion(version.asString.replace("pre", "beta"))
            return latest.compareTo(current)
        }
    }
}
