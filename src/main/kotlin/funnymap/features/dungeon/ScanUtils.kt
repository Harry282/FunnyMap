package funnymap.features.dungeon

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import funnymap.FunnyMap.Companion.mc
import funnymap.core.RoomData
import funnymap.core.map.Room
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import kotlin.math.roundToInt

object ScanUtils {
    val roomList: Set<RoomData> = try {
        Gson().fromJson(
            mc.resourceManager.getResource(
                ResourceLocation("funnymap", "rooms.json")
            ).inputStream.bufferedReader(), object : TypeToken<Set<RoomData>>() {}.type
        )
    } catch (e: JsonSyntaxException) {
        println("Error parsing FunnyMap room data.")
        setOf()
    } catch (e: JsonIOException) {
        println("Error reading FunnyMap room data.")
        setOf()
    }

    fun getRoomData(x: Int, z: Int): RoomData? {
        return getRoomData(getCore(x, z))
    }

    fun getRoomData(hash: Int): RoomData? {
        return roomList.find { hash in it.cores }
    }

    fun getRoomCentre(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = ((posX - DungeonScan.startX) / 32f).roundToInt()
        val roomZ = ((posZ - DungeonScan.startZ) / 32f).roundToInt()
        return Pair(roomX * 32 + DungeonScan.startX, roomZ * 32 + DungeonScan.startZ)
    }

    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - DungeonScan.startX + 15) shr 5)
        val z = ((pos.z - DungeonScan.startZ + 15) shr 5)
        val room = Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun getCore(x: Int, z: Int): Int {
        val sb = StringBuilder(150)
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(x, 0, z))
        for (y in 140 downTo 12) {
            val id = Block.blockRegistry.getIDForObject(chunk.getBlock(BlockPos(x, y, z)))
            if (!id.equalsOneOf(5, 54, 146)) {
                sb.append(id)
            }
        }
        return sb.toString().hashCode()
    }
}
