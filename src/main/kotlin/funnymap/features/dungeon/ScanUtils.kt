package funnymap.features.dungeon

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import funnymap.FunnyMap
import funnymap.core.RoomData
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation

object ScanUtils {
    val roomList: Set<RoomData> = try {
        Gson().fromJson(
            FunnyMap.mc.resourceManager.getResource(ResourceLocation("funnymap", "rooms.json"))
                .inputStream.bufferedReader(),
            object : TypeToken<Set<RoomData>>() {}.type
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
        val roomX = (posX - Dungeon.startX) shr 5
        val roomZ = (posZ - Dungeon.startZ) shr 5
        var x = 32 * roomX + Dungeon.startX
        if (x !in posX - 16..posX + 16) x += 32
        var z = 32 * roomZ + Dungeon.startZ
        if (z !in posZ - 16..posZ + 16) z += 32
        return Pair(x, z)
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
