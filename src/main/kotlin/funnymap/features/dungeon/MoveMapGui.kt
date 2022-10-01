package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.utils.RenderUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class MoveMapGui : GuiScreen() {

    private var startOffsetX = 0
    private var startOffsetY = 0
    private var isDragging = false
    private var resizingCorner: CornerButton? = null

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawDefaultBackground()
        updateCorners()
        draw(mouseX, mouseY)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            val corner = corners.firstOrNull { it.hovered }
            if (corner != null) {
                resizingCorner = corner
            } else if (hovered) {
                startOffsetX = mouseX - config.mapX
                startOffsetY = mouseY - config.mapY
                isDragging = true
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (clickedMouseButton == 0) {
            when (resizingCorner?.corner) {
                Corner.TOP_LEFT -> {
                    val s = max((x2 - mouseX) / w, (y2 - mouseY) / h)
                        .coerceIn(0.1..min(x2 / w, y2 / h))
                    config.mapX = (x2 - w * s).toInt()
                    config.mapY = ((y2 - h * s).toInt())
                    config.mapScale = s.toFloat()
                }

                Corner.TOP_RIGHT -> {
                    val s = max((mouseX - x) / w, (y2 - mouseY) / h)
                        .coerceIn(0.1..min((width - x) / w, y2 / h))
                    config.mapY = ((y2 - h * s).toInt())
                    config.mapScale = s.toFloat()
                }

                Corner.BOTTOM_LEFT -> {
                    val s = max((x2 - mouseX) / w, (mouseY - y) / h)
                        .coerceIn(0.1..min(x2 / w, (height - y) / h))
                    config.mapX = (x2 - w * s).toInt()
                    config.mapScale = s.toFloat()
                }

                Corner.BOTTOM_RIGHT -> {
                    val s = max((mouseX - x) / w, (mouseY - y) / h)
                        .coerceIn(0.1..min((width - x) / w, (height - y) / h))
                    config.mapScale = s.toFloat()
                }

                null -> if (isDragging) {
                    config.mapX = (mouseX - startOffsetX).coerceIn(0, (width - w * config.mapScale).toInt())
                    config.mapY = (mouseY - startOffsetY).coerceIn(0, (height - h * config.mapScale).toInt())
                }
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        isDragging = false
        resizingCorner = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun handleMouseInput() {
        var i = Mouse.getEventDWheel().coerceIn(-1..1)
        if (i != 0) {
            if (!isShiftKeyDown()) i *= 5
            config.mapScale = (config.mapScale + i * 0.01)
                .coerceIn(0.1..(min((width - x) / w, (height - y) / h))).toFloat()
        }
        super.handleMouseInput()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_LEFT -> config.mapX = (--config.mapX).coerceIn(0, (width - w * config.mapScale).toInt())
            Keyboard.KEY_RIGHT -> config.mapX = (++config.mapX).coerceIn(0, (width - w * config.mapScale).toInt())
            Keyboard.KEY_UP -> config.mapY = (--config.mapY).coerceIn(0, (height - h * config.mapScale).toInt())
            Keyboard.KEY_DOWN -> config.mapY = (++config.mapY).coerceIn(0, (height - h * config.mapScale).toInt())
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
        config.markDirty()
        config.writeData()
        super.onGuiClosed()
    }

    companion object {
        var corners = arrayOf(
            CornerButton(x - 3.0, y - 3.0, Corner.TOP_LEFT),
            CornerButton(x2 - 3.0, y - 3.0, Corner.TOP_RIGHT),
            CornerButton(x - 3.0, y2 - 3.0, Corner.BOTTOM_LEFT),
            CornerButton(x2 - 3.0, y2 - 3.0, Corner.BOTTOM_RIGHT)
        )
        val x: Double
            get() = config.mapX.toDouble()
        val y: Double
            get() = config.mapY.toDouble()
        val x2: Double
            get() = config.mapX + w * config.mapScale
        val y2: Double
            get() = config.mapY + h * config.mapScale
        val w: Double
            get() = 128.0
        val h: Double
            get() = (if (config.mapShowRunInformation) 138.0 else 128.0)
        var hovered = false

        fun draw(mouseX: Int, mouseY: Int) {
            hovered = mouseX.toFloat() in x..x2 && mouseY.toFloat() in y..y2
            MapRender.renderMap()
            RenderUtils.renderRectBorder(x, y, w * config.mapScale, h * config.mapScale, 0.5, Color(255, 255, 255))
            corners.forEach { it.draw(mouseX, mouseY) }
        }

        fun updateCorners() {
            corners[0].x = x - 3.0
            corners[0].y = y - 3.0
            corners[1].x = x2 - 3.0
            corners[1].y = y - 3.0
            corners[2].x = x - 3.0
            corners[2].y = y2 - 3.0
            corners[3].x = x2 - 3.0
            corners[3].y = y2 - 3.0
        }

        enum class Corner {
            TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
        }

        class CornerButton(var x: Double, var y: Double, val corner: Corner) {
            var hovered = false
            fun draw(mouseX: Int, mouseY: Int) {
                hovered = mouseX.toDouble() in x..x + 6.0 && mouseY.toDouble() in y..y + 6.0
                RenderUtils.renderRect(x, y, 6.0, 6.0, Color(255, 255, 255, 255))
            }
        }
    }
}
