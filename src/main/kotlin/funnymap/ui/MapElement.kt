package funnymap.ui

import funnymap.FunnyMap.Companion.config
import funnymap.features.dungeon.MapRender
import funnymap.utils.Location

class MapElement : MovableGuiElement() {
    override var x: Int
        get() = config.mapX
        set(value) {
            config.mapX = value
        }
    override var y: Int
        get() = config.mapY
        set(value) {
            config.mapY = value
        }
    override val h: Int
        get() = if (config.mapShowRunInformation) 138 else 128
    override val w: Int
        get() = 128
    override var x2: Int = (x + w * scale).toInt()
    override var y2: Int = (y + h * scale).toInt()
    override var scale: Float
        get() = config.mapScale
        set(value) {
            config.mapScale = value
        }

    override fun render() {
        MapRender.renderMap()
    }

    override fun shouldRender(): Boolean {
        if (!config.mapEnabled) return false
        if (config.mapHideInBoss && Location.inBoss) return false
        return super.shouldRender()
    }
}
