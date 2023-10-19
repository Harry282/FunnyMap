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
            val scoreColor = when {
                ScoreCalculation.score < 270 -> "§c"
                ScoreCalculation.score < 300 -> "§e"
                else -> "§a"
            }
            when (config.scoreTotalScore) {
                1 -> list.add("§7Score: §7($scoreColor${ScoreCalculation.score}§7)")
                2 -> list.add(
                    "§7Score: §b${ScoreCalculation.getSkillScore()}§7/" +
                            "§a${ScoreCalculation.getExplorationScore()}§7/" +
                            "§3${ScoreCalculation.getSpeedScore(RunInformation.timeElapsed)}§7/" +
                            "§d${ScoreCalculation.getBonusScore()} §7: " +
                            "§7($scoreColor${ScoreCalculation.score}§7)"
                )
            }

            when (config.scoreSecrets) {
                1 -> list.add("§7Secrets: §b${RunInformation.secretsFound}§7/§c${RunInformation.secretTotal}")
                2 -> {
                    val missing = (RunInformation.minSecrets - RunInformation.secretsFound).coerceAtLeast(0)
                    list.add("§7Secrets: §b${RunInformation.secretsFound}§7/§e${missing}§7/§c${RunInformation.secretTotal}")
                }
            }

            if (config.scoreCrypts) {
                val color = if (RunInformation.cryptsCount >= 5) "§a" else "§c"
                list.add("§7Crypts: $color${RunInformation.cryptsCount}")
            }

            if (config.scoreMimic) {
                list.add("§7Mimic:${if (RunInformation.mimicKilled) "§a ✔" else "§c ✘"}")
            }

            if (config.scoreDeaths) {
                list.add("§7Deaths: §c${RunInformation.deathCount}")
            }

            val completedPuzzles = RunInformation.totalPuzzles - RunInformation.missingPuzzles
            when (config.scorePuzzles) {
                1 -> list.add("§7Puzzles: §c$completedPuzzles")
                2 -> list.add("§7Puzzles: §c$completedPuzzles§7/§c${RunInformation.totalPuzzles}")
            }

            return list
        }

        fun runInformationLines(): List<String> {
            val list: MutableList<String> = mutableListOf()
            val scoreColor = when {
                ScoreCalculation.score < 270 -> "§c"
                ScoreCalculation.score < 300 -> "§e"
                else -> "§a"
            }
            if (config.runInformationScore) {
                list.add("§7Score: §7($scoreColor${ScoreCalculation.score}§7)")
            }

            when (config.runInformationSecrets) {
                1 -> list.add("§7Secrets: §b${RunInformation.secretsFound}§7/§c${RunInformation.secretTotal}")
                2 -> {
                    val missing = (RunInformation.minSecrets - RunInformation.secretsFound).coerceAtLeast(0)
                    list.add("§7Secrets: §b${RunInformation.secretsFound}§7/§e${missing}§7/§c${RunInformation.secretTotal}")
                }
            }

            list.add("split")

            if (config.runInformationCrypts) {
                val color = if (RunInformation.cryptsCount >= 5) "§a" else "§c"
                list.add("§7Crypts: $color${RunInformation.cryptsCount}")
            }

            if (config.runInformationMimic) {
                list.add("§7Mimic:${if (RunInformation.mimicKilled) "§a ✔" else "§c ✘"}")
            }

            if (config.runInformationDeaths) {
                list.add("§7Deaths: §c${RunInformation.deathCount}")
            }

            return list
        }
    }
}
