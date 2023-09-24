package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.events.ChatEvent
import funnymap.events.GuiContainerEvent
import funnymap.features.dungeon.Dungeon.Info.rooms
import funnymap.features.dungeon.Dungeon.Info.secretCount
import funnymap.features.dungeon.RunInformation.completed
import funnymap.features.dungeon.RunInformation.completedPuzzles
import funnymap.features.dungeon.RunInformation.cryptsCount
import funnymap.features.dungeon.RunInformation.firstDeath
import funnymap.features.dungeon.RunInformation.secretsFound
import funnymap.utils.Location
import funnymap.utils.Location.dungeonFloor
import funnymap.utils.Location.inBoss
import funnymap.utils.Location.masterMode
import funnymap.utils.Location.started
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.event.HoverEvent
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.*

object ScoreCalc {
    private val mimicRegexes = listOf("\$skytils-dungeon-score-mimic\$", "mimic killed!", "mimic dead!")
    private val speedNeeded = mapOf(
        "F1" to 600, "F2" to 600, "F3" to 600, "F4" to 720, "F5" to 600, "F6" to 720, "F7" to 840,
        "M1" to 480, "M2" to 480, "M3" to 480, "M4" to 480, "M5" to 480, "M6" to 600, "M7" to 840
    )

    var isPaul = false
    private val secretsPercentNeeded: Float
        get() = if (masterMode) 1f else (0.2f + dungeonFloor.coerceAtMost(5) * 0.1f +
                (dungeonFloor - 5).coerceAtLeast(0) * 0.15f)
    var mimicKilled = false
    var firstDeathHadSpirit = false
    private var bloodThing = false

    var score = 0
    private var scoresSent = 0
    var minSecrets = 0

    private var skillScore = 0
    private var explorationScore = 0
    private var speedScore = 0
    private var bonusScore = 0


    fun calcScore() {
        if (!Location.inDungeons ||! config.scoreCalc) return

        val deaths = (RunInformation.deathCount * 2 - (if (firstDeathHadSpirit) 1 else 0)).coerceAtLeast(0)
        if (!mimicKilled && secretsFound.toFloat() / secretCount == 100f) mimicKilled = true

        minSecrets = ceil(ceil(secretCount * secretsPercentNeeded) * ((40 - (if (isPaul) 10 else 0) - cryptsCount.coerceAtMost(5) - (if (mimicKilled) 2 else 0) + deaths) / 40f).coerceIn(0f, 40f)).toInt()
        val completedRooms = completed + (if (!inBoss) 1 else 0) + (if (bloodThing) 1 else 0)
        val roomPercentage = (completedRooms / rooms.toFloat()).coerceIn(0f, 1f)
        val secretsScore = floor(40f * secretsFound / secretCount / secretsPercentNeeded).coerceIn(0f, 40f)
        val timeElapsed = (ceil((started - System.currentTimeMillis()) / 1000f)).coerceAtLeast(0f)
        val requiredSpeed = speedNeeded["${if (masterMode) "M" else "F"}$dungeonFloor"] ?: 600
        val t = (timeElapsed - requiredSpeed) * (600f / requiredSpeed)
        val puzzles = 10f * (Dungeon.Info.puzzles.size - completedPuzzles.size).coerceAtLeast(0)

        skillScore = floor(20 + (80f * roomPercentage) - puzzles - deaths).toInt().coerceIn(20, 100)
        explorationScore = if (started == 0L) 0 else (floor(60f * roomPercentage) + secretsScore).toInt().coerceIn(0, 100)

        speedScore = floor(when {
            t <= 0 -> 100f
            t <= 120 -> 100f - t / 12f
            t <= 432 -> 95 - t / 24f
            t <= 1008 -> 89 - t / 36f
            t <= 3570 -> 85 - t / 42f
            else -> 0f
        }).toInt()
        bonusScore = cryptsCount.coerceAtMost(5) + (if (mimicKilled) 2 else 0) + (if (isPaul) 10 else 0)
        score = skillScore + explorationScore + speedScore + bonusScore
        scoreChange()
    }

    private fun scoreChange() {
        if (!config.scoreMessages || scoresSent == 2) return
        if (score >= 270) {
            if (scoresSent == 0 && (dungeonFloor < 5 || config.sendBothScores)) {
                scoresSent = 1
                notifyScore(config.lowerScoreMessage)
            }
            if (score >= 300 && scoresSent < 2 && (dungeonFloor > 4 || config.sendBothScores)) {
                scoresSent = 2
                notifyScore(config.higherScoreMessage)
            }
        }
    }

    private fun notifyScore(message: String) {
        val replaced = message.replace("&", "§")
        if (config.scoreNotifications == 1 || config.scoreNotifications == 3) Utils.showClientTitle(if (config.smallTitles) "" else replaced, if (config.smallTitles) replaced else "")
        if (config.scoreNotifications >= 2) Utils.modMessage(replaced)
        if (config.sendScoreMessages) mc.thePlayer.sendChatMessage("/pc ${message.replace(Regex("&\\S"), "")}")
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatMessage(event: ChatEvent) {
        if (!Location.inDungeons) return
        if (event.formatted.startsWith("§r§9Party §8>") && dungeonFloor.equalsOneOf(6, 7) && mimicRegexes.any { //&r&r&9Party &8> &a[VIP&6+&a] Vali_SUX&f: &rMimic Killed!&r
                event.text.lowercase().contains(it)
            }) mimicKilled = true
        else if (event.formatted.startsWith("§r§c ☠ §r§7You") && event.formatted.contains("and became a ghost§r§7.")) {
            if (!firstDeath && started > 0) {
                if (Dungeon.dungeonTeammates[mc.thePlayer.name]?.spiritPet == true) {
                    firstDeathHadSpirit = true
                }
                firstDeath = true
            }
        } else if (event.formatted == "§eEverybody unlocks §6exclusive §eperks! §a§l[HOVER TO VIEW]") {
            event.packet.chatComponent.chatStyle.chatHoverEvent.takeIf { it.action == HoverEvent.Action.SHOW_TEXT }?.value?.formattedText?.split(
                "\n"
            )?.takeIf { it.size > 1 }?.let { list ->
                isPaul = list.takeIf { list[0].contains("Mayor Paul") }
                    ?.find { it == "§r${list[0].substring(0, 2)}EZPZ" } != null
            } ?: run { isPaul = false }
        } else if (event.formatted.contains("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodThing = false
            return
        } else if (event.formatted.contains("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r")) {
            bloodThing = true
        } else return
        calcScore()
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlot) {
        if (event.gui.inventorySlots is ContainerChest) {
            (event.gui.inventorySlots as ContainerChest).lowerChestInventory.displayName.unformattedText.trim().let { name ->
                if (name == "Calendar and Events" || name.startsWith("Mayor ")) {
                    event.slot?.stack?.takeIf { it.item == Items.skull &&! it.displayName.contains("Election") && it.displayName.contains("Mayor ") }?.let { tooltip ->
                        tooltip.tagCompound?.takeIf { it.hasKey("display", 10) }?.getCompoundTag("display")?.getTagList("Lore", 8)?.let { tags ->
                            if (tooltip.displayName.contains("Mayor Paul")) {
                                val colorCode = tooltip.displayName.substring(0, 2)
                                for (i in 0 until tags.tagCount()) {
                                    if (tags.getStringTagAt(i) == "${colorCode}EZPZ") {
                                        isPaul = true
                                        return
                                    }
                                }
                            }
                            isPaul = false
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (event.entity is EntityZombie) {
            val entity = event.entity as EntityZombie
            if (entity.isChild && entity.getCurrentArmor(0) == null && entity.getCurrentArmor(1) == null && entity.getCurrentArmor(2) == null && entity.getCurrentArmor(3) == null) { //skytils
                if (!mimicKilled && dungeonFloor.equalsOneOf(6, 7)) {
                    mimicKilled = true
                    calcScore()
                    if (config.sendMimicFound) mc.thePlayer.sendChatMessage("/pc ${config.mimicMessage}")
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        mimicKilled = false
        MimicDetector.mimicRoom = null
        MimicDetector.mimicPos = null
        firstDeathHadSpirit = false
        bloodThing = false

        score = 0
        skillScore = 0
        explorationScore = 0
        speedScore = 0
        bonusScore = 0

        scoresSent = 0
        minSecrets = 0

        RunInformation.reset()
    }
}