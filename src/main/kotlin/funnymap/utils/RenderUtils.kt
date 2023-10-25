package funnymap.utils

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.features.dungeon.DungeonScan
import funnymap.features.dungeon.MapRender
import funnymap.utils.Utils.equalsOneOf
import funnymap.utils.Utils.itemID
import gg.essential.elementa.utils.withAlpha
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.Slot
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.StringUtils
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_LINE_STRIP
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import java.awt.Color

object RenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    private val playerPattern = Regex("(?:\\[.+?] )?(?<player>\\w+)")
    var selectedItem: Slot? = null
        set(value) {
            field = value
            hoveredPlayerName = value?.stack?.let { playerPattern.find(StringUtils.stripControlCodes(it.displayName))?.groups?.get("player")?.value }
        }
    private var hoveredPlayerName: String? = null

    private fun preDraw() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    }

    private fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    fun drawTexturedQuad(x: Double, y: Double, width: Double, height: Double) {
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }

    fun renderTriangle(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        worldRenderer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()

        tessellator.draw()

        postDraw()
    }

    fun drawBox(aabb: AxisAlignedBB, color: Color, outline: Float, outlineThickness: Float, fill: Float, ignoreDepth: Boolean) {
        GlStateManager.pushMatrix()
        preDraw()
        GlStateManager.depthMask(!ignoreDepth)
        GL11.glLineWidth(outlineThickness)

        drawOutlinedAABB(aabb, color.withAlpha(outline))

        drawFilledAABB(aabb, color.withAlpha(fill))

        GlStateManager.depthMask(true)
        postDraw()
        GlStateManager.popMatrix()
    }

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        color.bind()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        postDraw()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        color.bind()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)
        tessellator.draw()

        postDraw()
    }

    fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
        if (text.isEmpty()) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(config.textScale, config.textScale, 1f)

        if (config.mapDynamicRotate) {
            GlStateManager.rotate(-MapRender.dynamicRotation, 0f, 0f, 1f)
        }

        val fontHeight = mc.fontRendererObj.FONT_HEIGHT + 1
        val yTextOffset = text.size * fontHeight / -2f

        text.withIndex().forEach { (index, text) ->
            mc.fontRendererObj.drawString(
                text,
                mc.fontRendererObj.getStringWidth(text) / -2f,
                yTextOffset + index * 10,
                color,
                true
            )
        }

        if (config.mapDynamicRotate) {
            GlStateManager.rotate(MapRender.dynamicRotation, 0f, 0f, 1f)
        }

        GlStateManager.popMatrix()
    }

    fun drawPlayerHead(name: String, player: DungeonPlayer) {
        GlStateManager.pushMatrix()
        try {
            val selectedPlayer = name == hoveredPlayerName
            // Translates to the player's location which is updated every tick.
            if (name == mc.thePlayer.name && player.mapX == 0 && player.mapZ == 0) {
                GlStateManager.translate(
                    (mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first,
                    (mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second,
                    0.0
                )
            } else {
                GlStateManager.translate(player.mapX.toFloat(), player.mapZ.toFloat(), 0f)
            }

            // Handle player names
            if (name != mc.thePlayer.name && (config.playerHeads == 2 || config.playerHeads == 1 && mc.thePlayer.heldItem?.itemID.equalsOneOf(
                    "SPIRIT_LEAP", "INFINITE_SPIRIT_LEAP", "HAUNT_ABILITY"
                ))
            ) {
                GlStateManager.pushMatrix()
                GlStateManager.scale(0.8, 0.8, 1.0)
                if (config.mapRotate) {
                    GlStateManager.rotate(mc.thePlayer.rotationYawHead + 180f, 0f, 0f, 1f)
                }
                mc.fontRendererObj.drawString(
                    name, mc.fontRendererObj.getStringWidth(name) / -2f, 10f, 0xffffff, true
                )
                GlStateManager.popMatrix()
            }

            // Apply head rotation and scaling
            GlStateManager.rotate(player.yaw + 180f, 0f, 0f, 1f)
            GlStateManager.scale(config.playerHeadScale, config.playerHeadScale, 1f)
            if (selectedPlayer) GlStateManager.scale(1.35f, 1.35f, 1f)

            // Render black border around the player head
            renderRectBorder(-6.0, -6.0, 12.0, 12.0, 1.0, Color(0, if (selectedPlayer) 255 else 0, 0, 255))

            preDraw()
            GlStateManager.enableTexture2D()
            GlStateManager.color(1f, 1f, 1f, 1f)

            mc.textureManager.bindTexture(player.skin)

            Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)
            if (player.renderHat) {
                Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
            }

            postDraw()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }

    fun drawTextureAtPos(pos: BlockPos, location: ResourceLocation, size: Double, border: Boolean) {
        GlStateManager.pushMatrix()
        try {
            GlStateManager.translate(
                (pos.x - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2,
                (pos.z - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2,
                0.0
            )
            if (border) renderRectBorder(-size / 2.0, -size / 2.0, size, size, 1.0, Color(0, 0, 0, 255))

            preDraw()
            GlStateManager.enableTexture2D()
            GlStateManager.color(1f, 1f, 1f, 1f)

            mc.textureManager.bindTexture(location)

            drawTexturedQuad(-size / 2.0, -size / 2.0, size, size)

            postDraw()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }

    fun setColor(color: Color, index: Int, value: Int) : Color {
        return when (index) {
            0 -> Color(value, color.green, color.blue, color.alpha)
            1 -> Color(color.red, value, color.blue, color.alpha)
            else -> Color(color.red, color.green, value, color.alpha)
        }
    }

    fun Color.bind() {
        GlStateManager.color(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)
    }

    fun Entity.getInterpolatedPosition(partialTicks: Float): Triple<Double, Double, Double> {
        return Triple(
            this.lastTickPosX + (this.posX - this.lastTickPosX) * partialTicks,
            this.lastTickPosY + (this.posY - this.lastTickPosY) * partialTicks,
            this.lastTickPosZ + (this.posZ - this.lastTickPosZ) * partialTicks
        )
    }

    fun drawFilledAABB(aabb: AxisAlignedBB, color: Color) {
        color.bind()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()

        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawOutlinedAABB(aabb: AxisAlignedBB, color: Color) {
        color.bind()

        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()

        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()

        tessellator.draw()
    }
}
