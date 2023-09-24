package funnymap.mixins;

import funnymap.events.GuiContainerEvent;
import funnymap.utils.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
abstract class MixinGuiContainer extends GuiScreen {
    @Shadow private Slot theSlot;

    @Inject(method = "drawSlot", at = @At("TAIL"))
    void onDrawSlot(Slot slotIn, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawSlot(this, slotIn));
        if (theSlot != null && theSlot.getHasStack()) {
            RenderUtils.INSTANCE.setSelectedItem(theSlot);
        }
    }

    @Inject(method = "onGuiClosed", at = @At("TAIL"))
    public void onGuiClosed(CallbackInfo ci) {
        RenderUtils.INSTANCE.setSelectedItem(null);
    }
}
