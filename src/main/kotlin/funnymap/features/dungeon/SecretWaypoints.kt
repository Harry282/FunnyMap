package funnymap.features.dungeon

import funnymap.FunnyMap
import funnymap.FunnyMap.Companion.config
import funnymap.core.Secret.Companion.getSetting
import funnymap.utils.Location
import funnymap.utils.RenderUtils
import funnymap.utils.RenderUtils.getInterpolatedPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SecretWaypoints {
    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!Location.inDungeons || !config.secretWaypoints) return
        val (x, y, z) = FunnyMap.mc.renderViewEntity.getInterpolatedPosition(event.partialTicks)
        Location.currentRoom?.data?.name?.run {
            ScanUtils.secretList[this]?.forEach { secret ->
                val setting = secret.getSetting()
                if (setting.first) {
                    RenderUtils.drawBox(
                        Location.actualOfRelative(BlockPos(secret.x, secret.y, secret.z))?.let {
                            val pos = Vec3(it)
                            AxisAlignedBB(
                                pos.xCoord,
                                pos.yCoord,
                                pos.zCoord,
                                pos.xCoord + 1,
                                pos.yCoord + 1,
                                pos.zCoord + 1
                            )
                        }?.offset(-x, -y, -z) ?: return,
                        setting.second,
                        config.secretOutline,
                        config.secretOutlineThickness,
                        config.secretFill,
                        true
                    )
                }
            }
        }
    }
}