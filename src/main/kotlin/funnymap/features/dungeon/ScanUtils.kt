package funnymap.features.dungeon

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import funnymap.FunnyMap
import funnymap.core.Room
import funnymap.core.RoomData
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import kotlin.math.roundToInt

object ScanUtils {
    val roomList: Set<RoomData> = try {
        Gson().fromJson(
            FunnyMap.mc.resourceManager.getResource(
                ResourceLocation(
                    "funnymap",
                    "rooms.json"
                )
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
        val roomX = ((posX - Dungeon.startX) / 32f).roundToInt()
        val roomZ = ((posZ - Dungeon.startZ) / 32f).roundToInt()
        return Pair(roomX * 32 + Dungeon.startX, roomZ * 32 + Dungeon.startZ)
    }

    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - Dungeon.startX + 15) shr 5)
        val z = ((pos.z - Dungeon.startZ + 15) shr 5)
        val room = Dungeon.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun isColumnAir(x: Int, z: Int): Boolean {
        for (y in 12..140) {
            if (FunnyMap.mc.theWorld.getBlockState(BlockPos(x, y, z)).block != Blocks.air) {
                return false
            }
        }
        return true
    }

    fun isDoor(x: Int, z: Int): Boolean {
        val xPlus4 = isColumnAir(x + 4, z)
        val xMinus4 = isColumnAir(x - 4, z)
        val zPlus4 = isColumnAir(x, z + 4)
        val zMinus4 = isColumnAir(x, z - 4)
        return xPlus4 && xMinus4 && !zPlus4 && !zMinus4 || !xPlus4 && !xMinus4 && zPlus4 && zMinus4
    }

    fun getCore(x: Int, z: Int): Int {
        val blocks = arrayListOf<Int>()
        for (y in 140 downTo 12) {
            val id = Block.getIdFromBlock(FunnyMap.mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
            if (!id.equalsOneOf(5, 54)) {
                blocks.add(id)
            }
        }
        return blocks.joinToString("").hashCode()
    }
}
