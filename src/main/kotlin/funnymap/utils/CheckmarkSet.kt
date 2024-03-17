package funnymap.utils

import funnymap.core.map.RoomState
import funnymap.features.dungeon.MapRender
import net.minecraft.util.ResourceLocation

class CheckmarkSet(val size: Int, location: String) {
    private val crossResource = ResourceLocation("funnymap", "$location/cross.png")
    private val greenResource = ResourceLocation("funnymap", "$location/green_check.png")
    private val questionResource = ResourceLocation("funnymap", "$location/question.png")
    private val whiteResource = ResourceLocation("funnymap", "$location/white_check.png")

    fun getCheckmark(state: RoomState): ResourceLocation? {
        return when (state) {
            RoomState.CLEARED -> whiteResource
            RoomState.GREEN -> greenResource
            RoomState.FAILED -> crossResource
            RoomState.UNOPENED -> if (MapRender.legitRender) questionResource else null
            else -> null
        }
    }
}
