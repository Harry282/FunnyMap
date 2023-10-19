package funnymap.ui

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.features.dungeon.RunInformation
import funnymap.features.dungeon.ScoreCalculation
import funnymap.utils.Location
import net.minecraft.client.gui.FontRenderer

class ScoreElement : MovableGuiElement() {
    override var x: Int
        get() = config.scoreX
        set(value) {
            config.scoreX = value
        }
    override var y: Int
        get() = config.scoreY
        set(value) {
            config.scoreY = value
        }
    override val h: Int
        get() = fr.FONT_HEIGHT * elementLines
    override val w: Int = fr.getStringWidth("Score: 100/100/100/7 : (300)")
    override var x2: Int = (x + w * scale).toInt()
    override var y2: Int = (y + h * scale).toInt()
    override var scale: Float
        get() = config.scoreScale
        set(value) {
            config.scoreScale = value
        }

    private var elementLines = 1
        set(value) {
            if (field != value) {
                field = value
                y2 = (y + h * scale).toInt()
            }
        }

    override fun render() {
        var y = 0f
        val lines = getScoreLines()
        elementLines = lines.size
        lines.forEach {
            fr.drawString(it, 0f, y, 0xffffff, true)
            y += fr.FONT_HEIGHT
        }
    }

    override fun shouldRender(): Boolean {
        if (!config.scoreElementEnabled) return false
        if (config.scoreHideInBoss && Location.inBoss) return false
        return super.shouldRender()
    }

    companion object {
        val fr: FontRenderer = mc.fontRendererObj

        fun getScoreLines(): List<String> {
            val list: MutableList<String> = mutableListOf()

            when (config.scoreTotalScore) {
                1 -> list.add(getScore(false))
                2 -> list.add(getScore(true))
            }

            when (config.scoreSecrets) {
                1 -> list.add(getSecrets(false))
                2 -> list.add(getSecrets(true))
            }

            if (config.scoreCrypts) {
                list.add(getCrypts())
            }

            if (config.scoreMimic) {
                list.add(getMimic())
            }

            if (config.scoreDeaths) {
                list.add(getDeaths())
            }

            when (config.scorePuzzles) {
                1 -> list.add(getPuzzles(false))
                2 -> list.add(getPuzzles(true))
            }

            return list
        }

        fun runInformationLines(): List<String> {
            val list: MutableList<String> = mutableListOf()

            if (config.runInformationScore) {
                list.add(getScore(false))
            }

            when (config.runInformationSecrets) {
                1 -> list.add(getSecrets(false))
                2 -> list.add(getSecrets(true))
            }

            list.add("split")

            if (config.runInformationCrypts) {
                list.add(getCrypts())
            }

            if (config.runInformationMimic) {
                list.add(getMimic())
            }

            if (config.runInformationDeaths) {
                list.add(getDeaths())
            }

            return list
        }

        private fun getScore(expanded: Boolean): String {
            val scoreColor = when {
                ScoreCalculation.score < 270 -> "§c"
                ScoreCalculation.score < 300 -> "§e"
                else -> "§a"
            }
            var line = if (config.scoreMinimizedName) "" else "§7Score: "
            if (expanded) {
                line += "§b${ScoreCalculation.getSkillScore()}§7/" +
                        "§a${ScoreCalculation.getExplorationScore()}§7/" +
                        "§3${ScoreCalculation.getSpeedScore(RunInformation.timeElapsed)}§7/" +
                        "§d${ScoreCalculation.getBonusScore()} §7: "
            }
            line += "§7($scoreColor${ScoreCalculation.score}§7)"

            return line
        }

        private fun getSecrets(missing: Boolean): String {
            var line = if (config.scoreMinimizedName) "" else "§7Secrets: "
            line += "§b${RunInformation.secretsFound}§7/"
            if (missing) {
                val missingSecrets = (RunInformation.minSecrets - RunInformation.secretsFound).coerceAtLeast(0)
                line += "§e${missingSecrets}§7/"
            }
            line += "§c${RunInformation.secretTotal}"

            return line
        }

        private fun getCrypts(): String {
            var line = if (config.scoreMinimizedName) "§7C: " else "§7Crypts: "
            line += if (RunInformation.cryptsCount >= 5) "§a${RunInformation.cryptsCount}" else "§c${RunInformation.cryptsCount}"
            return line
        }

        private fun getMimic(): String {
            var line = if (config.scoreMinimizedName) "§7M: " else "§7Mimic: "
            line += if (RunInformation.mimicKilled) "§a✔" else "§c✘"
            return line
        }

        private fun getDeaths(): String {
            var line = if (config.scoreMinimizedName) "§7D: " else "§7Deaths: "
            line += "§c${RunInformation.deathCount}"
            return line
        }

        private fun getPuzzles(total: Boolean): String {
            val completedPuzzles = RunInformation.totalPuzzles - RunInformation.missingPuzzles
            var line = if (config.scoreMinimizedName) "§7P: " else "§7Puzzles: "
            line += "§c$completedPuzzles"
            if (total) line += "§7/§c${RunInformation.totalPuzzles}"
            return line
        }
    }
}
