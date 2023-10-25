package funnymap.core

import funnymap.core.map.RoomType

data class RoomData(
    val name: String,
    val type: RoomType,
    val cores: List<Int>,
    val crypts: Int,
    val secrets: Int,
    val trappedChests: Int,
    val dirCores: List<Int>?,
    val turn: List<Int>?,
    val distance: Int?,
    val strict: Boolean,
)
