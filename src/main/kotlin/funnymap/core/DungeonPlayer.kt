package funnymap.core

import com.google.gson.JsonObject
import funnymap.FunnyMap.Companion.scope
import funnymap.core.map.Room
import funnymap.features.dungeon.Dungeon
import funnymap.utils.APIUtils.getSecrets
import funnymap.utils.APIUtils.getSpirit
import funnymap.utils.APIUtils.loadPlayerData
import funnymap.utils.Location
import funnymap.utils.MapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation

data class DungeonPlayer(val skin: ResourceLocation) {

    var name = ""

    /** Minecraft formatting code for the player's name */
    var colorPrefix = 'f'

    /** The player's name with formatting code */
    val formattedName: String
        get() = "§$colorPrefix$name"

    var mapX = 0
    var mapZ = 0
    var yaw = 0f

    /** Has information from player entity been loaded */
    var playerLoaded = false
    var icon = ""
    var renderHat = false
    var dead = false
    var uuid = ""

    /** Stats for compiling player tracker information */
    private var playerData: JsonObject? = null
    var startingSecrets = 0
    var spiritPet = false
    var lastRoom = ""
    var lastTime = 0L
    var roomVisits: MutableList<Pair<Long, String>> = mutableListOf()
    var levers = 0
    var terminals = 0
    var devices = 0

    /** Set player data that requires entity to be loaded */
    fun setData(player: EntityPlayer) {
        renderHat = player.isWearing(EnumPlayerModelParts.HAT)
        uuid = player.uniqueID.toString()
        playerLoaded = true
        scope.launch(Dispatchers.IO) {
            playerData = loadPlayerData(uuid)?.apply {
                startingSecrets = getSecrets(this)
                spiritPet = getSpirit(this, uuid)
            }
        }
    }

    /** Gets the player's room, used for room tracker */
    fun getCurrentRoom(): String {
        if (dead) return "Dead"
        if (Location.inBoss) return "Boss"
        val x = (mapX - MapUtils.startCorner.first) / (MapUtils.mapRoomSize + 4)
        val z = (mapZ - MapUtils.startCorner.second) / (MapUtils.mapRoomSize + 4)
        return (Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22) as? Room)?.data?.name ?: "Error"
    }
}
