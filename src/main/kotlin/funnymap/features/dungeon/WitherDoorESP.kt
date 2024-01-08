package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.map.RoomState
import funnymap.utils.Location.inDungeons
import funnymap.utils.RenderUtils
import funnymap.utils.RenderUtils.getInterpolatedPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object WitherDoorESP {
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!inDungeons || config.witherDoorESP == 0) return

        mc.mcProfiler.startSection("funnymap-3d")

        val (x, y, z) = mc.renderViewEntity.getInterpolatedPosition(event.partialTicks)
        Dungeon.espDoors.forEach { door ->
            if (config.witherDoorESP == 1 && door.state == RoomState.UNDISCOVERED) return@forEach
            val aabb = AxisAlignedBB(door.x - 1.0, 69.0, door.z - 1.0, door.x + 2.0, 73.0, door.z + 2.0)
            RenderUtils.drawBox(
                aabb.offset(-x, -y, -z),
                if (Dungeon.Info.keys > 0) config.witherDoorKeyColor else config.witherDoorNoKeyColor,
                config.witherDoorOutlineWidth,
                config.witherDoorOutline,
                config.witherDoorFill,
                true
            )
        }

        mc.mcProfiler.endSection()
    }
}
