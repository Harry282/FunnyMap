package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.map.DoorType
import funnymap.utils.Location.inDungeons
import funnymap.utils.RenderUtils
import funnymap.utils.RenderUtils.getInterpolatedPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object WitherDoorESP {
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!inDungeons || !config.doorESP) return
        val (x, y, z) = mc.renderViewEntity.getInterpolatedPosition(event.partialTicks)
        Dungeon.espDoors.forEach { door ->
            val color = when {
                door.type == DoorType.BLOOD -> config.bloodDoorESPColor
                door.nextToFairy == true && door.opened -> config.fairyDoorESPColor
                else -> config.witherDoorESPColor
            }
            val aabb = AxisAlignedBB(door.x - 1.0, 69.0, door.z - 1.0, door.x + 2.0, 73.0, door.z + 2.0)
            RenderUtils.drawBox(
                aabb.offset(-x, -y, -z),
                color,
                config.doorOutline,
                config.doorOutlineThickness,
                config.doorFill,
                true
            )
        }
    }
}
