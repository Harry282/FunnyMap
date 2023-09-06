package funnymap.utils

import funnymap.FunnyMap.Companion.CHAT_PREFIX
import gg.essential.universal.UChat
import net.minecraft.item.ItemStack
import net.minecraft.util.StringUtils


object Utils {
    fun Any?.equalsOneOf(vararg other: Any): Boolean = other.any { this == it }

    fun String.removeFormatting(): String = StringUtils.stripControlCodes(this)

    fun modMessage(message: String) = UChat.chat("$CHAT_PREFIX $message")

    val ItemStack.itemID: String
        get() = this.getSubCompound("ExtraAttributes", false)?.getString("id") ?: ""
}
