package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.*
import funnymap.features.dungeon.ScanUtils.getRoomData
import funnymap.features.dungeon.ScanUtils.isDoor
import funnymap.utils.Utils.modMessage
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

object DungeonScan {
    fun scanDungeon() {
        var allLoaded = true
        val startTime = if (config.nanoScanTime) System.nanoTime() else System.currentTimeMillis()

        scan@ for (x in 0..10) {
            for (z in 0..10) {
                val xPos = Dungeon.startX + x * (Dungeon.roomSize shr 1)
                val zPos = Dungeon.startZ + z * (Dungeon.roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    allLoaded = false
                    continue@scan
                }
                if (Dungeon.dungeonList.getOrNull(z * 11 + x) != Door(0, 0)) continue
                getRoom(xPos, zPos, z, x)?.let {
                    if (it is Room && x and 1 == 0 && z and 1 == 0) Dungeon.rooms.add(it)
                    if (it is Door && it.type == DoorType.WITHER) Dungeon.doors[it] = Pair(x, z)
                    Dungeon.dungeonList[z * 11 + x] = it
                }
            }
        }

        if (allLoaded) {
            Dungeon.hasScanned = true
            MapUpdate.calibrate()

            if (config.scanChatInfo) {
                val scanTime = if (config.nanoScanTime) {
                    "&b${System.nanoTime() - startTime}&ans!"
                } else {
                    "&b${System.currentTimeMillis() - startTime}&ams!"
                }
                val lines = listOf(
                    "&aScan Finished! Took $scanTime",
                    "&aPuzzles (&c${Dungeon.puzzles.size}&a):",
                    Dungeon.puzzles.joinToString(separator = "\n&b- &d", prefix = "&b- &d"),
                    "&6Trap: &a${Dungeon.trapType}",
                    "&8Wither Doors: &7${Dungeon.doors.size - 1}",
                    "&7Total Secrets: &b${Dungeon.secretCount}"
                )
                modMessage(lines.joinToString(separator = "\n"))
            }
        }
    }

    private fun getRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val roomCore = ScanUtils.getCore(x, z)
        // Empty air column
        if (roomCore == -318865360) return null

        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> {
                Room(x, z, getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
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
                        bState.block == Blocks.coal_block -> DoorType.WITHER
                        bState.block == Blocks.monster_egg -> DoorType.ENTRANCE
                        bState.block == Blocks.stained_hardened_clay && Blocks.stained_hardened_clay.getMetaFromState(
                            bState
                        ) == 14 -> DoorType.BLOOD

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
}
