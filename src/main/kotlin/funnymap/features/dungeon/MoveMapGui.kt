package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import net.minecraft.client.gui.GuiScreen

class MoveMapGui : GuiScreen() {

    private var startOffsetX = 0
    private var startOffsetY = 0
    private var isDragging = false

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && isCursorOnMap(mouseX, mouseY)) {
            startOffsetX = mouseX - config.mapX
            startOffsetY = mouseY - config.mapY
            isDragging = true
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (clickedMouseButton == 0 && isDragging) {
            config.mapX = mouseX - startOffsetX
            config.mapY = mouseY - startOffsetY
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        isDragging = false
        super.mouseReleased(mouseX, mouseY, state)
    }

    private fun isCursorOnMap(mouseX: Int, mouseY: Int): Boolean {
        val mapSize = 128 * config.mapScale
        return mouseX in config.mapX..(config.mapX + mapSize).toInt() && mouseY in config.mapY..(config.mapY + mapSize).toInt()
    }
}
