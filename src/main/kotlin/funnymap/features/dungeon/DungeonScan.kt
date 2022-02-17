package funnymap.features.dungeon

import com.google.gson.Gson
import com.google.gson.JsonElement
import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.*
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import java.io.BufferedReader
import java.io.InputStreamReader

object DungeonScan {

    private val roomList: List<RoomData> = try {
        Gson().fromJson(
            BufferedReader(
                InputStreamReader(
                    mc.resourceManager.getResource(
                        ResourceLocation("funnymap", "rooms.json")
                    ).inputStream
                )
            ).readText(),
            JsonElement::class.java
        ).asJsonObject["rooms"].asJsonArray.map { jsonElement ->
            val room = jsonElement.asJsonObject
            RoomData(
                room["name"].asString,
                RoomType.valueOf(room["type"].asString),
                room["secrets"].asInt,
                room["cores"].asJsonArray.map { it.asInt }
            )
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        listOf()
    }

    fun scanDungeon() {
        Dungeon.reset()
        var allLoaded = true
        val startTime = System.currentTimeMillis()

        scan@
        for (x in 0..10) {
            for (z in 0..10) {
                val xPos = Dungeon.startX + x * (Dungeon.roomSize shr 1)
                val zPos = Dungeon.startZ + z * (Dungeon.roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    allLoaded = false
                    break@scan
                }
                if (isColumnAir(xPos, zPos)) continue

                getRoom(xPos, zPos, z, x)?.let {
                    if (it is Room && x and 1 == 0 && z and 1 == 0) Dungeon.rooms.add(it)
                    Dungeon.dungeonList[z * 11 + x] = it
                }
            }
        }

        if (allLoaded) {
            Dungeon.hasScanned = true
            MapUpdate.calibrate()

            if (config.scanChatInfo) {
                Utils.modMessage(
                    "&aScan Finished! Took &b${System.currentTimeMillis() - startTime}&ams!\n" +
                            "&aPuzzles (&c${Dungeon.puzzles.size}&a):${
                                Dungeon.puzzles.joinToString(
                                    "\n&b- &d",
                                    "\n&b- &d",
                                    "\n"
                                )
                            }" +
                            "&6Trap: &a${Dungeon.trapType}\n" +
                            " &8Wither Doors: &7${Dungeon.witherDoors - 1}\n" +
                            " &7Total Secrets: &b${Dungeon.secretCount}"
                )
            }
        } else Dungeon.reset()
    }

    private fun getRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> {
                getRoomData(x, z)?.let {
                    Room(x, z, it).apply {
                        if (Dungeon.uniqueRooms.none { match -> match.data.name == data.name }) {
                            Dungeon.uniqueRooms.add(this)
                            Dungeon.secretCount += data.secrets
                            when (data.type) {
                                RoomType.TRAP -> Dungeon.trapType = data.name.split(" ")[0]
                                RoomType.PUZZLE -> Dungeon.puzzles.add(data.name)
                                else -> {}
                            }
                        }
                    }
                }
            }
            !rowEven && !columnEven -> {
                Dungeon.dungeonList[(row - 1) * 11 + column - 1].let {
                    if (it is Room) {
                        Room(x, z, it.data).apply { isSeparator = true }
                    } else null
                }
            }
            isDoor(x, z) -> {
                Door(x, z).apply {
                    val bState = mc.theWorld.getBlockState(BlockPos(x, 69, z))
                    type = when {
                        bState.block == Blocks.coal_block -> {
                            Dungeon.witherDoors++
                            DoorType.WITHER
                        }
                        bState.block == Blocks.monster_egg -> DoorType.ENTRANCE
                        bState.block == Blocks.stained_hardened_clay &&
                                Blocks.stained_hardened_clay.getMetaFromState(bState) == 14 -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                }
            }
            else -> {
                Dungeon.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it is Room) {
                        if (it.data.type == RoomType.ENTRANCE) {
                            Door(x, z).apply { type = DoorType.ENTRANCE }
                        } else {
                            Room(x, z, it.data).apply { isSeparator = true }
                        }
                    } else null
                }
            }
        }
    }

    private fun getRoomData(x: Int, z: Int): RoomData? {
        return getRoomData(getCore(x, z))
    }

    private fun getRoomData(hash: Int): RoomData? {
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

    private fun isColumnAir(x: Int, z: Int): Boolean {
        for (y in 12..140) {
            if (mc.theWorld.getBlockState(BlockPos(x, y, z)).block != Blocks.air) {
                return false
            }
        }
        return true
    }

    private fun isDoor(x: Int, z: Int): Boolean {
        val xPlus4 = isColumnAir(x + 4, z)
        val xMinus4 = isColumnAir(x - 4, z)
        val zPlus4 = isColumnAir(x, z + 4)
        val zMinus4 = isColumnAir(x, z - 4)
        return xPlus4 && xMinus4 && !zPlus4 && !zMinus4 || !xPlus4 && !xMinus4 && zPlus4 && zMinus4
    }

    private fun getCore(x: Int, z: Int): Int {
        val blocks = arrayListOf<Int>()
        for (y in 140 downTo 12) {
            val id = Block.getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
            if (!id.equalsOneOf(5, 54)) {
                blocks.add(id)
            }
        }
        return blocks.joinToString("").hashCode()
    }
}
