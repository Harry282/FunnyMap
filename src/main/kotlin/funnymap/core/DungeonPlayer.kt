package funnymap.core

import funnymap.FunnyMap.Companion.scope
import funnymap.core.map.Room
import funnymap.features.dungeon.Dungeon
import funnymap.utils.APIUtils
import funnymap.utils.LocationUtils
import funnymap.utils.MapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation

data class DungeonPlayer(val skin: ResourceLocation) {

    var name = ""
    var colorPrefix = 'f'
    val formattedName: String
        get() = "§$colorPrefix$name"

    var mapX = 0
    var mapZ = 0
    var yaw = 0f

    var renderHat = false
    var icon = ""
    var dead = false
    var uuid = ""
    var startingSecrets = 0
    var playerLoaded = false

    var lastRoom = ""
    var lastTime = 0L
    var roomVisits: MutableList<Pair<Long, String>> = mutableListOf()

    fun setData(player: EntityPlayer) {
        renderHat = player.isWearing(EnumPlayerModelParts.HAT)
        uuid = player.uniqueID.toString()
        playerLoaded = true
        scope.launch(Dispatchers.IO) {
            startingSecrets = APIUtils.getSecrets(uuid)
        }
    }

    fun getCurrentRoom(): String {
        if (dead) return "Dead"
        if (LocationUtils.inBoss) return "Boss"
        val x = (mapX - MapUtils.startCorner.first) / (MapUtils.mapRoomSize + 4)
        val z = (mapZ - MapUtils.startCorner.second) / (MapUtils.mapRoomSize + 4)
        return (Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22) as? Room)?.data?.name ?: "Error"
    }
}
