package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.scope
import funnymap.core.DungeonPlayer
import funnymap.utils.APIUtils
import funnymap.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object PlayerTracker {

    fun onDungeonEnd() {
        scope.launch {
            val data = Dungeon.dungeonTeammates.map { (name, player) ->
                async(Dispatchers.IO) { Triple(name, player, APIUtils.getSecrets(player.uuid)) }
            }
            data.forEach {
                val (name, player, secrets) = it.await()
                sendStatMessage(name, player, secrets)
            }
        }
    }

    private fun sendStatMessage(name: String, player: DungeonPlayer, secrets: Int) {
        Utils.modMessage("§3$name got §b${secrets - player.startingSecrets} §3secrets")
    }
}
