package funnymap.features.dungeon

import net.minecraft.client.network.NetworkPlayerInfo

object RunInformation {

    var deathCount = 0
    var secretsFound = 0
    var cryptsCount = 0
    var completed = 0
    var completedPuzzles = 0
    var firstDeath = false

    private val deathsPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")
    private val completedPattern = Regex("§r Completed Rooms: §r§d(?<amount>\\d+)§r")
    private val completedPuzzlePattern = Regex("§r .+: §r§7\\[§r§a§l✔§r§7] §.+")

    fun updateRunInformation(tabEntries: List<Pair<NetworkPlayerInfo, String>>) {
        var changed = false
        completedPuzzles = 0
        tabEntries.forEach {
            val text = it.second
            when {
                text.contains("Deaths: ") -> {
                    val matcher = deathsPattern.find(text) ?: return@forEach
                    deathCount = matcher.groups["deaths"]?.value?.toIntOrNull()?.also { int -> if (deathCount != int) changed = true } ?: deathCount
                }

                text.contains("Secrets Found: ") && !text.contains("%") -> {
                    val matcher = secretsFoundPattern.find(text) ?: return@forEach
                    secretsFound = matcher.groups["secrets"]?.value?.toIntOrNull()?.also { int -> if (secretsFound != int) changed = true } ?: secretsFound
                }

                text.contains("Crypts: ") -> {
                    val matcher = cryptsPattern.find(text) ?: return@forEach
                    cryptsCount = matcher.groups["crypts"]?.value?.toIntOrNull()?.also { int -> if (cryptsCount != int) changed = true } ?: cryptsCount
                }

                text.contains("Completed Rooms: ") -> {
                    val matcher = completedPattern.find(text) ?: return@forEach
                    completed = matcher.groups["amount"]?.value?.toIntOrNull()?.also { int -> if (completed != int) changed = true } ?: completed
                }

                text.contains("✔") -> {
                    completedPuzzlePattern.find(text) ?: return@forEach
                    completedPuzzles++
                    changed = true
                }
            }
        }
        if (changed) ScoreCalc.calcScore()
    }
}