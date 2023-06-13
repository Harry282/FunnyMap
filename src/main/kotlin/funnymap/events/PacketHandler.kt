package funnymap.events

import funnymap.FunnyMap.Companion.mc
import funnymap.config.Config
import funnymap.core.map.RoomType
import funnymap.features.dungeon.Ghostblocks
import funnymap.features.dungeon.ScanUtils
import funnymap.utils.LocationUtils
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S21PacketChunkData
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

object PacketHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onPacketReceived(event: PacketReceivedEvent) { //does not update oldblocks
        if (event.packet is S02PacketChat) MinecraftForge.EVENT_BUS.post(ChatEvent(event.packet))
        if (!Config.GBToggle ||! Config.cancelPackets || Ghostblocks.stopRendering) return
        if (event.packet is S21PacketChunkData) {
            val x = (event.packet.chunkX shl 4) + 7
            val z = (event.packet.chunkZ shl 4) + 7
            if ((LocationUtils.inDungeons && LocationUtils.currentRoom != null && ScanUtils.getRoomFromPos(Pair(x, z))?.data?.name == LocationUtils.currentRoom?.data?.name)
                || ((LocationUtils.inBoss) && (abs(x - mc.thePlayer.posX) / 16f) <= Config.cancelPacketsRange && (abs(z - mc.thePlayer.posZ) / 16f) <= Config.cancelPacketsRange))
                Ghostblocks.render()
        } else if (event.packet is S22PacketMultiBlockChange) {
            val map: MutableMap<BlockPos, IBlockState> = mutableMapOf()
            event.packet.changedBlocks.forEach { map[it.pos] = it.blockState }
            val oldList = Ghostblocks.oldBlocks[Ghostblocks.getName()]?.filter { map.keys.contains(it.pos) }?.map { it.pos }
            if (oldList.isNullOrEmpty()) return
            event.isCanceled = true
            map.forEach { block -> if (!oldList.contains(block.key)) mc.theWorld.setBlockState(block.key, block.value) }
        } else if (event.packet is S23PacketBlockChange) {
            if (Ghostblocks.oldBlocks[Ghostblocks.getName()]?.find { it.pos == event.packet.blockPosition } != null) event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketSendEvent) {
        if (!Config.GBToggle ||! Config.cancelPackets || Config.rightClickReset == 0) return
        if (mc.thePlayer != null && (Config.rightClickReset == 1 || LocationUtils.currentRoom?.data?.type == RoomType.TRAP)) {
            if (event.packet is C07PacketPlayerDigging) {
                val pos = event.packet.position
                if (Ghostblocks.restored.remove(pos)) {
                    event.isCanceled = true
                    val block = Ghostblocks.oldBlocks[Ghostblocks.getName()]?.find { it.pos == pos } ?: return
                    if (block.c == 0) Ghostblocks.ghostblock(pos, true)
                    else Ghostblocks.placeblock(pos)
                }
            }
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) { //this rather than sendPacket because of event.face
        if (!Config.GBToggle ||! Config.cancelPackets || Config.rightClickReset == 0) return
        if (event.entity == mc.thePlayer && (Config.rightClickReset == 1 || LocationUtils.currentRoom?.data?.type == RoomType.TRAP)) {
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                val state = event.world.getBlockState(event.pos).block
                if (Ghostblocks.illegalBlocks.contains(state) && state != Blocks.skull) return
                val pos = event.pos.offset( event.face ?: mc.objectMouseOver?.sideHit ?: return)
                val block = Ghostblocks.oldBlocks[Ghostblocks.getName()]?.find { it.pos == pos } ?: return
                if (mc.theWorld.setBlockState(block.pos, block.state)) {
                    Ghostblocks.restored.add(block.pos)
                    event.isCanceled = true //not truly necessary
                }
            }
        }
    }
}