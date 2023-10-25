package funnymap.utils

import funnymap.FunnyMap.Companion.CHAT_PREFIX
import gg.essential.universal.UChat
import funnymap.FunnyMap.Companion.mc
import net.minecraft.item.ItemStack
import net.minecraft.util.StringUtils


object Utils {
    fun Any?.equalsOneOf(vararg other: Any): Boolean = other.any { this == it }

    fun String.removeFormatting(): String = StringUtils.stripControlCodes(this)

    fun modMessage(message: String) = UChat.chat("$CHAT_PREFIX $message")

    fun showClientTitle(title: String, subtitle: String) {
        mc.ingameGUI.displayTitle(null, null, 2, 40, 2)
        mc.ingameGUI.displayTitle(null, subtitle, -1, -1, -1)
        mc.ingameGUI.displayTitle(title, null, -1, -1, -1)
    }

    val ItemStack.itemID: String
        get() = this.getSubCompound("ExtraAttributes", false)?.getString("id") ?: ""
}
