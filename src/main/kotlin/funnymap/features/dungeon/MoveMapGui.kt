package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import net.minecraft.client.gui.GuiScreen

class MoveMapGui : GuiScreen() {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.drawDefaultBackground()
        MapRender.renderRooms()
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        config.mapX = mouseX
        config.mapY = mouseY
    }
}
