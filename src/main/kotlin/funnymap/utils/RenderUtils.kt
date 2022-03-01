package funnymap.utils

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.features.dungeon.Dungeon
import funnymap.utils.Utils.itemID
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EnumPlayerModelParts
import org.lwjgl.opengl.GL11
import java.awt.Color

object RenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        GlStateManager.disableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

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

    fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
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

    fun drawPlayerHead(player: DungeonPlayer, multiplier: Double) {
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
}
