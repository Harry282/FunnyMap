package funnymap.features.dungeon

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import funnymap.FunnyMap.Companion.mc
import funnymap.core.RoomData
import funnymap.core.map.Direction
import funnymap.core.map.Room
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import java.awt.Point
import java.util.TreeSet
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
        return getRoomFromPos(Pair(pos.x, pos.z))
    }
    fun getRoomFromPos(pos: Pair<Int, Int>): Room? {
        val x = ((pos.first - DungeonScan.startX + 15) shr 5)
        val z = ((pos.second - DungeonScan.startZ + 15) shr 5)
        val room = Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun getCore(x: Int, z: Int): Int {
        val blocks = arrayListOf<Int>()
        for (y in 140 downTo 12) {
            val id = Block.getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
            if (!id.equalsOneOf(5, 54)) {
                blocks.add(id)
            }
        }
        return blocks.joinToString("").hashCode()
    }

    fun getPosCore(direction: Direction?, x: Int, z: Int, distance: Int): Int {
        val rtp = Point(x, z)
        when (direction) {
            Direction.NW -> rtp.translate(-distance, -distance)
            Direction.NE -> rtp.translate(distance, -distance)
            Direction.SE -> rtp.translate(distance, distance)
            Direction.SW -> rtp.translate(-distance, distance)
            else -> { return 0 }
        }
        return getCore(rtp.x, rtp.y)
    }

    fun getDirection(x: Int, z: Int, data: RoomData, roomCore: Int): Direction? {
        if (data.dirCores != null) {
            val distance = data.distance?: 4
            for (direction in Direction.values()) {
                val core = getPosCore(direction, x, z, distance)
                if (data.dirCores.contains(core) && core != 0) {
                    val index = data.dirCores.indexOf(core)
                    if (!data.strict || data.cores.indexOf(roomCore) == index) {
                        if (data.turn != null) return Direction.values()[(Direction.values().indexOf(direction) + data.turn[index]) % 4]
                        return direction
                    }
                }
            }
        }
        return null
    }

    fun getCorner(direction: Direction?, name: String) : Point? { //does not check for amount of segments
        val xSet: TreeSet<Int> = TreeSet()
        val zSet: TreeSet<Int> = TreeSet()
        Dungeon.Info.dungeonList.forEach {
            if (it is Room) {
                if (it.data.name == name) {
                    xSet.add(it.x)
                    zSet.add(it.z)
                }
            }
        }
        if (xSet.size < 1) return null
        return when (direction) {
            Direction.NW -> Point(xSet.first() - 15, zSet.first() - 15)
            Direction.NE -> Point(xSet.last() + 15, zSet.first() - 15)
            Direction.SE -> Point(xSet.last() + 15, zSet.last() + 15)
            Direction.SW -> Point(xSet.first() - 15, zSet.last() + 15)
            else -> null
        }
    }
}
