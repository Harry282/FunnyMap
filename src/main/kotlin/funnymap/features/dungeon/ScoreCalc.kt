package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.mc
import funnymap.config.Config
import funnymap.events.ChatEvent
import funnymap.features.dungeon.Dungeon.Info.rooms
import funnymap.features.dungeon.Dungeon.Info.secretCount
import funnymap.features.dungeon.RunInformation.completed
import funnymap.features.dungeon.RunInformation.completedPuzzles
import funnymap.features.dungeon.RunInformation.cryptsCount
import funnymap.features.dungeon.RunInformation.firstDeath
import funnymap.features.dungeon.RunInformation.secretsFound
import funnymap.utils.APIUtils
import funnymap.utils.LocationUtils
import funnymap.utils.LocationUtils.dungeonFloor
import funnymap.utils.LocationUtils.inBoss
import funnymap.utils.LocationUtils.masterMode
import funnymap.utils.LocationUtils.started
import funnymap.utils.Utils
import net.minecraft.entity.monster.EntityZombie
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.regex.Pattern
import kotlin.math.*

object ScoreCalc {
    private val mimicRegexes = listOf("\$skytils-dungeon-score-mimic$", "mimic killed!", "mimic dead!")
    private val speedNeeded = mapOf(
        "F1" to 600, "F2" to 600, "F3" to 600, "F4" to 720, "F5" to 600, "F6" to 720, "F7" to 840,
        "M1" to 480, "M2" to 480, "M3" to 480, "M4" to 480, "M5" to 480, "M6" to 600, "M7" to 840
    )

    var isPaul = false

    var higherFloor = false
    var secretsPercentNeeded = 1f
    var mimicKilled = false
    var firstDeathHadSpirit = false
    var bloodThing = false

    var score = 0
    private var scoresSent = 0
    var minSecrets = 0

    var skillScore = 0
    var explorationScore = 0
    var speedScore = 0
    var bonusScore = 0


    fun calcScore() {
        if (!LocationUtils.inDungeons ||! Config.scoreCalc) return

        val deaths = max(RunInformation.deathCount * 2 - (if (firstDeathHadSpirit) 1 else 0), 0)

        minSecrets = ceil(ceil(secretCount * secretsPercentNeeded) * ((40 - (if (isPaul) 10 else 0) - min(cryptsCount, 5) - (if (mimicKilled) 2 else 0) + deaths) / 40f).coerceIn(0f, 40f)).toInt()
        val completedRooms = completed + if (!inBoss) 1 else 0 + if (bloodThing) 1 else 0
        val roomPercentage = (completedRooms.toFloat() / rooms).coerceIn(0f, 1f)
        val secretsScore = floor(40f * secretsFound / secretCount / secretsPercentNeeded).coerceIn(0f, 40f)
        val timeElapsed = max(ceil((started - System.currentTimeMillis()) / 1000f), 0f)
        val requiredSpeed = speedNeeded["${if (masterMode) "M" else "F"}$dungeonFloor"] ?: 600
        val t = (timeElapsed - requiredSpeed) * (600f / requiredSpeed)
        val puzzles = 10f * (Dungeon.Info.puzzles.size - completedPuzzles).coerceAtLeast(0)

        skillScore = floor(20 + (80f * roomPercentage) - puzzles - deaths).toInt().coerceIn(20, 100)
        explorationScore = if (started == 0L) 0 else min(100, (floor(60f * roomPercentage) + secretsScore).toInt())
        speedScore = floor(when {
            t <= 0 -> 100f
            t <= 120 -> 100f - t / 12f
            t <= 432 -> 95 - t / 24f
            t <= 1008 -> 89 - t / 36f
            t <= 3570 -> 85 - t / 42f
            else -> 0f
        }).toInt()
        bonusScore = min(cryptsCount, 5) + (if (mimicKilled) 2 else 0) + (if (isPaul) 10 else 0)
        score = skillScore + explorationScore + speedScore + bonusScore
        scoreChange()
    }

    private fun scoreChange() {
        if (!Config.scoreMessages || scoresSent == 2) return
        if (score >= 270) {
            if (scoresSent == 0 && (!higherFloor || Config.sendBothScores)) {
                scoresSent = 1
                notifyScore(Config.lowerScoreMessage)
            }
            if (score >= 300 && scoresSent < 2 && (higherFloor || Config.sendBothScores)) {
                scoresSent = 2
                notifyScore(Config.higherScoreMessage)
            }
        }
    }

    fun notifyScore(message: String) {
        if (Config.scoreNotifications == 1 || Config.scoreNotifications == 3) Utils.showClientTitle("", message.replace("&", "§"))
        if (Config.scoreNotifications >= 2) Utils.modMessage(message.replace("&", "§"))
        if (Config.sendScoreMessages) mc.thePlayer.sendChatMessage("/pc ${message.replace(Regex("&\\S"), "")}")
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatMessage(event: ChatEvent) {
        if (event.formatted.startsWith("§r§r§9Party §8>") && mimicRegexes.contains(event.text.lowercase())) mimicKilled = true
        else if (event.formatted.startsWith("§r§c ☠ §r§7You") && event.formatted.contains("and became a ghost§r§7.")) {
            if (!firstDeath && started > 0) {
                if (Dungeon.dungeonTeammates[mc.thePlayer.name]?.spiritPet == true) {
                    firstDeathHadSpirit = true
                }
                firstDeath = true
            }
        } else if (Pattern.compile("Elections for year \\d+ have ended").matcher(event.formatted).find()) {
            if (!isPaul) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        APIUtils.getMayor()
                    }
                }, 300000)
            } else isPaul = false
        } else if (event.formatted.contains("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodThing = false
        } else if (event.text.contains("The BLOOD DOOR has been opened!") || event.formatted.startsWith("§r§c[BOSS] The Watcher§r§f")) {
            bloodThing = true
        } else return
        calcScore()
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (event.entity is EntityZombie) {
            val entity = event.entity as EntityZombie
            if (entity.isChild && entity.getCurrentArmor(0) == null && entity.getCurrentArmor(1) == null && entity.getCurrentArmor(2) == null && entity.getCurrentArmor(3) == null) { //skytils
                if (!mimicKilled) {
                    mimicKilled = true
                    calcScore()
                    if (Config.sendMimicFound) mc.thePlayer.sendChatMessage("/pc ${Config.mimicMessage}")
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        mimicKilled = false
        firstDeathHadSpirit = false
        bloodThing = false

        score = 0
        scoresSent = 0
        minSecrets = 0

        MimicDetector.roomName = null
        firstDeath = false
    }
}