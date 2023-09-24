package funnymap.events

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Event

abstract class GuiContainerEvent(val gui: GuiContainer) : Event() {
    class DrawSlot(gui: Any, val slot: Slot?) : GuiContainerEvent(gui as GuiContainer)
}