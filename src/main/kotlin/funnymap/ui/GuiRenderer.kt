package funnymap.ui

import funnymap.FunnyMap.Companion.mc
import funnymap.utils.Location
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object GuiRenderer {
    val elements = mutableListOf(
        MapElement(),
        ScoreElement()
    )

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !Location.inDungeons) return
        if (mc.currentScreen is EditLocationGui) return
        mc.entityRenderer.setupOverlayRendering()
        elements.forEach {
            if (!it.shouldRender()) return@forEach
            GlStateManager.pushMatrix()
            GlStateManager.translate(it.x.toFloat(), it.y.toFloat(), 0f)
            GlStateManager.scale(it.scale, it.scale, 1f)
            it.render()
            GlStateManager.popMatrix()
        }
    }
}
