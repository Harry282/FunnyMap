package funnymap.ui

import funnymap.FunnyMap.Companion.config
import funnymap.features.dungeon.MapRender
import funnymap.utils.Location

class MapElement : MovableGuiElement() {
    override var x: Int by config::mapX
    override var y: Int by config::mapY
    override val h: Int
        get() = if (config.mapShowRunInformation) 142 else 128
    override val w: Int
        get() = 128
    override var scale: Float by config::mapScale
    override var x2: Int = (x + w * scale).toInt()
    override var y2: Int = (y + h * scale).toInt()

    override fun render() {
        MapRender.renderMap()
    }

    override fun shouldRender(): Boolean {
        if (!config.mapEnabled) return false
        if (config.mapHideInBoss && Location.inBoss) return false
        return super.shouldRender()
    }
}
