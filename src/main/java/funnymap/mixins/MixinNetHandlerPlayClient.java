package funnymap.mixins;

import funnymap.events.PacketReceivedEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
abstract class MixinNetHandlerPlayClient {
    @Inject(method = "handleChunkData", at = @At("TAIL"))
    private void onChunkUpdate(S21PacketChunkData packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketReceivedEvent(packet));
    }

    @Inject(method = "handleMultiBlockChange", at = @At("TAIL"))
    private void onMultiBlockChange(S22PacketMultiBlockChange packet, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PacketReceivedEvent(packet));
    }
}
