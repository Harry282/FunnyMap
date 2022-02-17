package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.inDungeons
import funnymap.FunnyMap.Companion.mc
import funnymap.core.*
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MapRender {

    private const val roomSize = 20

    private val neuGreen = ResourceLocation("funnymap", "neu/green_check.png")
    private val neuWhite = ResourceLocation("funnymap", "neu/white_check.png")
    private val neuCross = ResourceLocation("funnymap", "neu/cross.png")
    private val defaultGreen = ResourceLocation("funnymap", "default/green_check.png")
    private val defaultWhite = ResourceLocation("funnymap", "default/white_check.png")
    private val defaultCross = ResourceLocation("funnymap", "default/cross.png")

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || !inDungeons || !config.mapEnabled) return
        if (config.mapHideInBoss && Dungeon.inBoss || !Dungeon.hasScanned) return
        if (mc.currentScreen is MoveMapGui) return

        renderRooms()

        renderText()

        renderPlayerHeads()
    }

    fun renderRooms() {
        val mapScale = config.mapScale

        GlStateManager.pushMatrix()
        GlStateManager.translate(config.mapX.toFloat(), config.mapY.toFloat(), 0f)
        GlStateManager.scale(mapScale, mapScale, 1f)

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = Dungeon.dungeonList[y * 11 + x]
                if (tile is Door && tile.type == DoorType.NONE) continue

                val xOffset = (x shr 1) * 26
                val yOffset = (y shr 1) * 26

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                val color = if (config.mapDarkenUndiscovered && tile.state == RoomState.UNDISCOVERED) tile.getColor()
                    .darker() else tile.getColor()

                when {
                    xEven && yEven -> if (tile is Room) {
                        Gui.drawRect(
                            xOffset,
                            yOffset,
                            xOffset + roomSize,
                            yOffset + roomSize,
                            color.rgb
                        )
                    }
                    !xEven && !yEven -> {
                        Gui.drawRect(
                            xOffset + roomSize,
                            yOffset + roomSize,
                            xOffset + 26,
                            yOffset + 26,
                            color.rgb
                        )
                    }
                    else -> drawRoomConnector(xOffset, yOffset, tile is Door, !xEven, color.rgb)
                }
            }
        }

        GlStateManager.popMatrix()
    }

    private fun renderText() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(config.mapX.toFloat(), config.mapY.toFloat(), 0f)
        GlStateManager.scale(config.mapScale, config.mapScale, 1f)


        for (y in 0..10 step 2) {
            for (x in 0..10 step 2) {

                val tile = Dungeon.dungeonList[y * 11 + x]

                if (tile is Room && tile in Dungeon.uniqueRooms) {

                    val xOffset = (x shr 1) * 26
                    val yOffset = (y shr 1) * 26

                    if (config.mapCheckmark != 0 && config.mapRoomSecrets != 2) {
                        GlStateManager.color(255f, 255f, 255f)
                        getCheckmark(tile.state, config.mapCheckmark)?.let {
                            mc.textureManager.bindTexture(it)
                            when (config.mapCheckmark) {
                                1 -> Gui.drawModalRectWithCustomSizedTexture(
                                    xOffset + 4, yOffset + 4,
                                    0f, 0f, 12, 12, 12f, 12f
                                )
                                2 -> Gui.drawModalRectWithCustomSizedTexture(
                                    xOffset + 2, yOffset + 2,
                                    0f, 0f, 16, 16, 16f, 16f
                                )
                                else -> {}
                            }
                        }
                    }

                    val name = mutableListOf<String>()

                    if (config.mapRoomNames != 0 && (tile.data.type == RoomType.PUZZLE || tile.data.type == RoomType.TRAP) ||
                        config.mapRoomNames == 2 && tile.data.type == RoomType.NORMAL
                    ) {
                        name.addAll(tile.data.name.split(" "))
                    }
                    if (tile.data.type == RoomType.NORMAL && config.mapRoomSecrets == 1) {
                        name.add(tile.data.secrets.toString())
                    }

                    val color = if (config.mapColorText) when (tile.state) {
                        RoomState.GREEN -> 0x55ff55
                        RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                        else -> 0xaaaaaa
                    } else 0xffffff

                    // Offset + half of roomsize
                    drawCenteredText(name, xOffset + 10f, yOffset + 10f, color)
                }
            }
        }

        GlStateManager.popMatrix()
    }

    private fun getCheckmark(state: RoomState, type: Int): ResourceLocation? {
        return when (type) {
            1 -> when (state) {
                RoomState.CLEARED -> defaultWhite
                RoomState.GREEN -> defaultGreen
                RoomState.FAILED -> defaultCross
                else -> null
            }
            2 -> when (state) {
                RoomState.CLEARED -> neuWhite
                RoomState.GREEN -> neuGreen
                RoomState.FAILED -> neuCross
                else -> null
            }
            else -> null
        }
    }

    private fun renderPlayerHeads() {
        for (player in Dungeon.dungeonTeamates) {
            drawPlayerHead(player)
        }
    }

    private fun drawCenteredText(text: List<String>, x: Float, y: Float, color: Int) {
        GlStateManager.pushMatrix()

        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(config.textScale, config.textScale, 1f)

        if (text.isNotEmpty()) {
            val yTextOffset = 5f * text.size
            for (i in text.indices) {
                mc.fontRendererObj.drawString(
                    text[i],
                    -mc.fontRendererObj.getStringWidth(text[i]) / 2f,
                    -yTextOffset + i * 10f,
                    color,
                    true
                )
            }
        }
        GlStateManager.popMatrix()
    }

    private fun drawPlayerHead(player: DungeonPlayer) {
        GlStateManager.pushMatrix()
        try {
            GlStateManager.translate(
                ((player.x - Dungeon.startX) / Dungeon.roomSize * 26 + 8) * config.mapScale + config.mapX,
                ((player.z - Dungeon.startZ) / Dungeon.roomSize * 26 + 8) * config.mapScale + config.mapY,
                0.0
            )
            GlStateManager.rotate(player.yaw + 180f, 0f, 0f, 1f)
            mc.textureManager.bindTexture(mc.netHandler.getPlayerInfo(player.player.uniqueID).locationSkin)
            val pos = (-6 * config.mapScale).toInt()
            val size = (12 * config.mapScale).toInt()
            Gui.drawScaledCustomSizeModalRect(pos, pos, 8f, 8f, 8, 8, size, size, 64f, 64f)
            if (player.player.isWearing(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect(pos, pos, 40f, 8f, 8, 8, size, size, 64f, 64f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }

    private fun drawRoomConnector(x: Int, y: Int, doorway: Boolean, vertical: Boolean, color: Int) {
        // 8 x 12 or 8 x 20 connector
        val doorwayOffset = 6
        val length = 6
        val width = if (doorway) 8 else roomSize
        var x1 = if (vertical) x + roomSize else x
        var y1 = if (vertical) y else y + roomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        val x2 = if (vertical) x1 + length else x1 + width
        val y2 = if (vertical) y1 + width else y1 + length
        Gui.drawRect(x1, y1, x2, y2, color)
    }
}
