package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.inDungeons
import funnymap.FunnyMap.Companion.mc
import funnymap.core.*
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.roomSize
import funnymap.utils.Utils.equalsOneOf
import funnymap.utils.Utils.itemID
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object MapRender {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    private val neuGreen = ResourceLocation("funnymap", "neu/green_check.png")
    private val neuWhite = ResourceLocation("funnymap", "neu/white_check.png")
    private val neuCross = ResourceLocation("funnymap", "neu/cross.png")
    private val defaultGreen = ResourceLocation("funnymap", "default/green_check.png")
    private val defaultWhite = ResourceLocation("funnymap", "default/white_check.png")
    private val defaultCross = ResourceLocation("funnymap", "default/cross.png")

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !inDungeons || !config.mapEnabled) return
        if (config.mapHideInBoss && Dungeon.inBoss || !Dungeon.hasScanned) return

        val x = config.mapX.toDouble()
        val y = config.mapY.toDouble()
        val height = if (config.mapShowRunInformation) (128.0 + 20.0) * config.mapScale else 128.0 * config.mapScale
        val width = 128.0 * config.mapScale
        val thickness = config.mapBorderWidth.toDouble()

        renderMapBackground(x, y, width, height, config.mapBackground)
        renderMapBorder(x, y, width, height, thickness, config.mapBorder)

        GlStateManager.pushMatrix()
        GlStateManager.translate(config.mapX.toFloat(), config.mapY.toFloat(), 0f)
        GlStateManager.scale(config.mapScale, config.mapScale, 1f)
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        renderRooms()
        if (mc.currentScreen !is MoveMapGui) {
            renderText()
            renderPlayerHeads()
            if (config.mapShowRunInformation) {
                renderRunInformation()
            }
        }
        GlStateManager.popMatrix()
    }

    private fun renderMapBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.red / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        GlStateManager.shadeModel(GL11.GL_FLAT)

        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)

        tessellator.draw()

        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    private fun renderMapBackground(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.red / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }


    private fun renderRooms() {
        val connectorSize = roomSize shr 2

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = Dungeon.dungeonList[y * 11 + x]
                if (tile is Door && tile.type == DoorType.NONE) continue

                val xOffset = (x shr 1) * (roomSize + connectorSize)
                val yOffset = (y shr 1) * (roomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                val color = if (config.mapDarkenUndiscovered && tile.state == RoomState.UNDISCOVERED) {
                    tile.getColor().run {
                        Color(
                            (red * (1 - config.mapDarkenPercent)).toInt(),
                            (green * (1 - config.mapDarkenPercent)).toInt(),
                            (blue * (1 - config.mapDarkenPercent)).toInt(),
                            alpha
                        )
                    }
                } else tile.getColor()

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
                            xOffset + (roomSize + connectorSize),
                            yOffset + (roomSize + connectorSize),
                            color.rgb
                        )
                    }
                    else -> drawRoomConnector(
                        xOffset,
                        yOffset,
                        connectorSize,
                        tile is Door,
                        !xEven,
                        color.rgb
                    )
                }
            }
        }

    }

    private fun renderText() {
        val connectorSize = roomSize shr 2

        for (y in 0..10 step 2) {
            for (x in 0..10 step 2) {

                val tile = Dungeon.dungeonList[y * 11 + x]

                if (tile is Room && tile in Dungeon.uniqueRooms) {

                    val xOffset = (x shr 1) * (roomSize + connectorSize)
                    val yOffset = (y shr 1) * (roomSize + connectorSize)

                    if (config.mapCheckmark != 0 && config.mapRoomSecrets != 2) {
                        getCheckmark(tile.state, config.mapCheckmark)?.let {
                            GlStateManager.enableAlpha()
                            GlStateManager.color(255f, 255f, 255f, 255f)
                            mc.textureManager.bindTexture(it)
                            Gui.drawModalRectWithCustomSizedTexture(
                                xOffset + 2, yOffset + 2,
                                0f, 0f, roomSize - 4, roomSize - 4, roomSize - 4f, roomSize - 4f
                            )
                            GlStateManager.disableAlpha()
                        }
                    }

                    val name = mutableListOf<String>()

                    if (config.mapRoomNames != 0 && tile.data.type.equalsOneOf(RoomType.PUZZLE, RoomType.TRAP) ||
                        config.mapRoomNames == 2 && tile.data.type.equalsOneOf(
                            RoomType.NORMAL,
                            RoomType.RARE,
                            RoomType.CHAMPION
                        )
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
                    drawCenteredText(name, xOffset + (roomSize shr 1), yOffset + (roomSize shr 1), color)
                }
            }
        }
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
        val multiplier = (roomSize + 4.0) / Dungeon.roomSize
        for (player in Dungeon.dungeonTeamates) {
            drawPlayerHead(player, multiplier)
        }
    }

    private fun drawCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
        GlStateManager.pushMatrix()

        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(config.textScale, config.textScale, 1f)

        if (text.isNotEmpty()) {
            val yTextOffset = text.size * 5f
            for (i in text.indices) {
                mc.fontRendererObj.drawString(
                    text[i],
                    (-mc.fontRendererObj.getStringWidth(text[i]) shr 1).toFloat(),
                    i * 10 - yTextOffset,
                    color,
                    true
                )
            }
        }
        GlStateManager.popMatrix()
    }

    private fun drawPlayerHead(player: DungeonPlayer, multiplier: Double) {
        if (player.dead) return
        GlStateManager.pushMatrix()
        try {
            GlStateManager.translate(
                (player.x - Dungeon.startX) * multiplier + 8,
                (player.z - Dungeon.startZ) * multiplier + 8,
                0.0
            )
            if (config.playerHeads == 2 || config.playerHeads == 1 && mc.thePlayer.heldItem?.itemID == "SPIRIT_LEAP") {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.8, 0.8, 1.0)
                mc.fontRendererObj.drawString(
                    player.name,
                    -mc.fontRendererObj.getStringWidth(player.name) shr 1,
                    10,
                    0xffffff
                )
                GlStateManager.popMatrix()
            }
            GlStateManager.rotate(player.yaw + 180f, 0f, 0f, 1f)
            GlStateManager.scale(config.playerHeadScale, config.playerHeadScale, 1f)
            Gui.drawRect(-7, -7, 7, 7, 0x000000)
            GlStateManager.color(255f, 255f, 255f)
            mc.textureManager.bindTexture(mc.netHandler.getPlayerInfo(player.player.uniqueID).locationSkin)
            Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)
            if (player.player.isWearing(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }

    private fun drawRoomConnector(
        x: Int,
        y: Int,
        doorWidth: Int,
        doorway: Boolean,
        vertical: Boolean,
        color: Int
    ) {
        val doorwayOffset = if (roomSize == 16) 5 else 6
        val width = if (doorway) 6 else roomSize
        var x1 = if (vertical) x + roomSize else x
        var y1 = if (vertical) y else y + roomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        val x2 = if (vertical) x1 + doorWidth else x1 + width
        val y2 = if (vertical) y1 + width else y1 + doorWidth
        Gui.drawRect(x1, y1, x2, y2, color)
    }

    private fun renderRunInformation() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, 128f, 0f)
        GlStateManager.scale(0.6, 0.6, 1.0)
        mc.fontRendererObj.drawString("Secrets: ${RunInformation.secretCount}/${Dungeon.secretCount}", 0, 0, 0xffffff)
        mc.fontRendererObj.drawString("Crypts: ${RunInformation.cryptsCount}", 90, 0, 0xffffff)
        mc.fontRendererObj.drawString("Deaths: ${RunInformation.deathCount}", 150, 0, 0xffffff)
        GlStateManager.popMatrix()
    }
}
