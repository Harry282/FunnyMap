package funnymap.core

import funnymap.FunnyMap.Companion.config
import java.awt.Color

data class Secret (
    val secretName: String,
    val category: SecretCategory,
    val x: Int,
    val y: Int,
    val z: Int
) {
    enum class SecretCategory {
        bat, chest, entrance, fairysoul, item, lever, puzzle, stonk, superboom, wither
    }
    companion object {
        fun Secret.getSetting() : Pair<Boolean, Color> {
            return when (this.category) {
                SecretCategory.bat -> Pair(true, Color.orange)
                SecretCategory.chest -> Pair(true, Color.cyan)
                SecretCategory.entrance -> Pair(config.entranceWaypoints, Color.green)
                SecretCategory.fairysoul -> Pair(config.fairySoulWaypoints, Color.pink)
                SecretCategory.item -> Pair(true, Color.blue)
                SecretCategory.lever -> Pair(config.leverWaypoints, Color.yellow)
                SecretCategory.puzzle -> Pair(true, Color.gray)
                SecretCategory.stonk -> Pair(config.stonkWaypoints, Color.magenta)
                SecretCategory.superboom -> Pair(config.superboomWaypoints, Color.red)
                SecretCategory.wither -> Pair(true, Color.black)
            }
        }
    }
}