package funnymap.features.dungeon

import net.minecraft.client.network.NetworkPlayerInfo

object RunInformation {

    var deathCount = 0
    var secretsFound = 0
    var cryptsCount = 0
    var completed = 0
    var completedPuzzles : ArrayList<String> = arrayListOf()
    var firstDeath = false

    private val deathsPattern = Regex("§r§a§lTeam Deaths: §r§f(?<deaths>\\d+)§r")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")
    private val completedPattern = Regex("§r Completed Rooms: §r§d(?<amount>\\d+)§r")
    private val completedPuzzlePattern = Regex("§r (?<name>.+): §r§7\\[§r§a§l✔§r§7] §.+")

    fun updateRunInformation(player: NetworkPlayerInfo) {
        val text = player.displayName?.formattedText ?: player.gameProfile?.name ?: return
        when {
            text.contains("Deaths: ") -> {
                val matcher = deathsPattern.find(text) ?: return
                matcher.groups["deaths"]?.value?.toIntOrNull()?.run { deathCount = this } ?: return
            }

            text.contains("Secrets Found: ") && !text.contains("%") -> {
                val matcher = secretsFoundPattern.find(text) ?: return
                matcher.groups["secrets"]?.value?.toIntOrNull()?.run { secretsFound = this } ?: return
            }

            text.contains("Crypts: ") -> {
                val matcher = cryptsPattern.find(text) ?: return
                matcher.groups["crypts"]?.value?.toIntOrNull()?.run { cryptsCount = this } ?: return
            }

            text.contains("Completed Rooms: ") -> {
                val matcher = completedPattern.find(text) ?: return
                matcher.groups["amount"]?.value?.toIntOrNull()?.run { completed = this } ?: return
            }

            text.contains("✔") -> {
                completedPuzzlePattern.find(text)?.let {
                    it.groups["name"]?.value?.takeIf { !completedPuzzles.contains(it) }?.run {
                        completedPuzzles.add(this)
                    } ?: return
                } ?: return
            }

            else -> return
        }
        ScoreCalc.calcScore()
    }

    fun reset() {
        deathCount = 0
        secretsFound = 0
        cryptsCount = 0
        completed = 0
        completedPuzzles.clear()
        firstDeath = false
    }
}