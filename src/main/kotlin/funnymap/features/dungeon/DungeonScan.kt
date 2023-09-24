package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.map.*
import funnymap.features.dungeon.DungeonScan.scan
import funnymap.utils.Location.dungeonFloor
import funnymap.utils.Location.setCurrentRoom
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

/**
 * Handles everything related to scanning the dungeon. Running [scan] will update the instance of [Dungeon].
 */
object DungeonScan {

    /**
     * The size of each dungeon room in blocks.
     */
    const val roomSize = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val startX = -185
    const val startZ = -185

    private var lastScanTime = 0L
    var isScanning = false
    var hasScanned = false

    val shouldScan: Boolean
        get() = config.autoScan && !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && dungeonFloor != -1

    fun scan() {
        isScanning = true
        var allChunksLoaded = true

        // Scans the dungeon in a 11x11 grid.
        for (x in 0..10) {
            for (z in 0..10) {
                // Translates the grid index into world position.
                val xPos = startX + x * (roomSize shr 1)
                val zPos = startZ + z * (roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    // The room being scanned has not been loaded in.
                    allChunksLoaded = false
                    continue
                }

                // This room has already been added in a previous scan.
                if (Dungeon.Info.dungeonList[x + z * 11] !is Unknown) continue

                scanRoom(xPos, zPos, z, x)?.let {
                    Dungeon.Info.dungeonList[z * 11 + x] = it
                }
            }
        }
        setCurrentRoom(ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt()))

        if (allChunksLoaded) {
            if (config.scanChatInfo) {
                val lines = mutableListOf(
                    "&aScan Finished!",
                    "&aPuzzles (&c${Dungeon.Info.puzzles.size}&a):",
                    Dungeon.Info.puzzles.joinToString(separator = "\n&b- &d", prefix = "&b- &d"),
                    "&6Trap: &a${Dungeon.Info.trapType}",
                    "&8Wither Doors: &7${Dungeon.Info.witherDoors - 1}",
                    "&7Total Crypts: &6${Dungeon.Info.cryptCount}",
                    "&7Total Secrets: &b${Dungeon.Info.secretCount}"
                )
                Utils.modMessage(lines.joinToString(separator = "\n"))
            }
            hasScanned = true
        }

        isScanning = false
    }

    private fun scanRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val height = mc.theWorld.getChunkFromChunkCoords(x shr 4, z shr 4).getHeightValue(x and 15, z and 15)
        if (height == 0) return null

        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            // Scanning a room
            rowEven && columnEven -> {
                val roomCore = ScanUtils.getCore(x, z)
                Room(x, z, ScanUtils.getRoomData(roomCore) ?: return null).apply {
                    Dungeon.Info.rooms++
                    core = roomCore
                    direction = ScanUtils.getDirection(x, z, data, roomCore)
                    if (data.type == RoomType.FAIRY) Dungeon.Info.fairyPos = Pair(x, z)

                    // Checks if a room with the same name has already been scanned.
                    val duplicateRoom = Dungeon.Info.uniqueRooms.firstOrNull { it.data.name == data.name }

                    if (duplicateRoom == null) {
                        // Adds room information if no duplicate was found
                        Dungeon.Info.uniqueRooms.add(this)
                        Dungeon.Info.cryptCount += data.crypts
                        Dungeon.Info.secretCount += data.secrets
                        when (data.type) {
                            RoomType.ENTRANCE -> MapRender.dynamicRotation = when {
                                row == 0 -> 180f
                                column == 0 -> -90f
                                column > row -> 90f
                                else -> 0f
                            }
                            RoomType.TRAP -> Dungeon.Info.trapType = data.name.split(" ")[0]
                            RoomType.PUZZLE -> Dungeon.Info.puzzles.add(data.name)
                            else -> {}
                        }
                    } else {
                        if (direction == null) direction = duplicateRoom.direction
                        if (x < duplicateRoom.x || (x == duplicateRoom.x && z < duplicateRoom.z)) {
                            // Ensures the room stored in uniqueRooms is the furthest south-east.
                            Dungeon.Info.uniqueRooms.remove(duplicateRoom)
                            Dungeon.Info.uniqueRooms.add(this)
                        }
                    }
                }
            }

            // Can only be the center "block" of a 2x2 room.
            !rowEven && !columnEven -> {
                Dungeon.Info.dungeonList[column - 1 + (row - 1) * 11].let {
                    if (it is Room) Room(x, z, it.data).apply { isSeparator = true } else null
                }
            }

            // Doorway between rooms
            // Old trap has a single block at 82
            height.equalsOneOf(74, 82) -> {
                Door(x, z).apply {
                    // Finds door type from door block
                    type = when (mc.theWorld.getBlockState(BlockPos(x, 69, z)).block) {
                        Blocks.coal_block -> {
                            Dungeon.Info.witherDoors++
                            DoorType.WITHER
                        }

                        Blocks.monster_egg -> DoorType.ENTRANCE
                        Blocks.stained_hardened_clay -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                }
            }

            // Connection between large rooms
            else -> {
                Dungeon.Info.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it !is Room) {
                        null
                    } else if (it.data.type == RoomType.ENTRANCE) {
                        Door(x, z).apply { type = DoorType.ENTRANCE }
                    } else {
                        Room(x, z, it.data).apply { isSeparator = true }
                    }
                }
            }
        }
    }
}
