package funnymap.features.dungeon

import funnymap.FunnyMap.Companion.config
import funnymap.FunnyMap.Companion.mc
import funnymap.core.DungeonPlayer
import funnymap.core.map.*
import funnymap.utils.Location.dungeonFloor
import funnymap.utils.Location.inBoss
import funnymap.utils.Location.inDungeons
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapRoomSize
import funnymap.utils.RenderUtils
import funnymap.utils.Utils
import funnymap.utils.Utils.equalsOneOf
import gg.essential.elementa.utils.withAlpha
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object MapRender {

    private val neuGreen = ResourceLocation("funnymap", "neu/green_check.png")
    private val neuWhite = ResourceLocation("funnymap", "neu/white_check.png")
    private val neuCross = ResourceLocation("funnymap", "neu/cross.png")
    private val defaultGreen = ResourceLocation("funnymap", "default/green_check.png")
    private val defaultWhite = ResourceLocation("funnymap", "default/white_check.png")
    private val defaultCross = ResourceLocation("funnymap", "default/cross.png")
    private val mimicHead = ResourceLocation("funnymap", "mimichead.png")

    var dynamicRotation = 0f

    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || !inDungeons || !config.mapEnabled) return
        if (inBoss && (config.mapHideInBoss == 2 || (!config.mapShowRunInformation && config.mapHideInBoss == 1))) return
        if (mc.currentScreen is MoveMapGui) return
        mc.entityRenderer.setupOverlayRendering()
        renderMap()
    }

    fun renderMap() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(config.mapX.toFloat(), config.mapY.toFloat(), 0f)
        GlStateManager.scale(config.mapScale, config.mapScale, 1f)

        val height = (if (config.mapShowRunInformation) if (config.scoreCalc) 148.0 else 138.0 else 128.0) - (if (inBoss && config.mapHideInBoss == 1) 128.0 else 0.0)
        RenderUtils.renderRect(
            0.0, 0.0, 128.0, height, config.mapBackground
        )

        RenderUtils.renderRectBorder(
            0.0,
            0.0,
            128.0,
            height,
            config.mapBorderWidth.toDouble(),
            config.mapBorder
        )

        if (!inBoss || config.mapHideInBoss == 0) {
            if (config.mapRotate) {
                GlStateManager.pushMatrix()
                setupRotate()
            } else if (config.mapDynamicRotate) {
                GlStateManager.translate(64.0, 64.0, 0.0)
                GlStateManager.rotate(dynamicRotation, 0f, 0f ,1f)
                GlStateManager.translate(-64.0, -64.0, 0.0)
            }

            renderRooms()
            renderText()
            renderEntities()
            if (!inBoss) renderPlayerHeads()

            if (config.mapRotate) {
                GL11.glDisable(GL11.GL_SCISSOR_TEST)
                GlStateManager.popMatrix()
            } else if (config.mapDynamicRotate) {
                GlStateManager.translate(64.0, 64.0, 0.0)
                GlStateManager.rotate(-dynamicRotation, 0f, 0f ,1f)
                GlStateManager.translate(-64.0, -64.0, 0.0)
            }
        }

        if (config.mapShowRunInformation) {
            renderRunInformation()
        }

        GlStateManager.popMatrix()
    }

    private fun setupRotate() {
        val scale = ScaledResolution(mc).scaleFactor
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(
            (config.mapX * scale),
            (mc.displayHeight - config.mapY * scale - 128 * scale * config.mapScale).toInt(),
            (128 * scale * config.mapScale).toInt(),
            (128 * scale * config.mapScale).toInt()
        )
        GlStateManager.translate(64.0, 64.0, 0.0)
        GlStateManager.rotate(-mc.thePlayer.rotationYawHead + 180f, 0f, 0f, 1f)

        if (config.mapCenter) {
            GlStateManager.translate(
                -((mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first - 2),
                -((mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second - 2),
                0.0
            )
        } else {
            GlStateManager.translate(-64.0, -64.0, 0.0)
        }
    }

    private fun renderRooms() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = mapRoomSize shr 2

        for (y in 0..10) {
            for (x in 0..10) {
                val tile = Dungeon.Info.dungeonList[y * 11 + x]
                if (tile is Door && tile.type == DoorType.NONE) continue

                val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                val color = if (config.mapDarkenUndiscovered && tile.state == RoomState.UNDISCOVERED) {
                    tile.color.run {
                        Color(
                            (red * (1 - config.mapDarkenPercent)).toInt(),
                            (green * (1 - config.mapDarkenPercent)).toInt(),
                            (blue * (1 - config.mapDarkenPercent)).toInt(),
                            (alpha * config.mapRoomTransparency).toInt()
                        )
                    }
                } else tile.color.run { withAlpha((alpha * config.mapRoomTransparency).toInt()) }

                when {
                    xEven && yEven -> if (tile is Room) {
                        RenderUtils.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            mapRoomSize.toDouble(),
                            mapRoomSize.toDouble(),
                            color
                        )
                    }

                    !xEven && !yEven -> {
                        RenderUtils.renderRect(
                            xOffset.toDouble(),
                            yOffset.toDouble(),
                            (mapRoomSize + connectorSize).toDouble(),
                            (mapRoomSize + connectorSize).toDouble(),
                            color
                        )
                    }

                    else -> drawRoomConnector(
                        xOffset, yOffset, connectorSize, tile is Door, !xEven, color
                    )
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private fun renderText() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(MapUtils.startCorner.first.toFloat(), MapUtils.startCorner.second.toFloat(), 0f)

        val connectorSize = mapRoomSize shr 2
        val checkmarkSize = when (config.mapCheckmark) {
            1 -> 8.0 // default
            else -> 10.0 // neu
        }

        for (y in 0..10 step 2) {
            for (x in 0..10 step 2) {

                val tile = Dungeon.Info.dungeonList[y * 11 + x]

                if (tile is Room && tile in Dungeon.Info.uniqueRooms) {
                    val unique = Dungeon.Info.uniqueRooms.find { it.data.name == tile.data.name } //I did not want to search in dungeonList every time it updates secrets or gets LocationUtils.currentRoom
                    if (unique != null) tile.secretsfound = unique.secretsfound

                    val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                    val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                    if (config.mapCheckmark != 0 && config.mapRoomSecrets != 2) {
                        if (config.mapCheckmark == 3) {
                            if (tile.data.type != RoomType.ENTRANCE) {
                                val color: Color = when (tile.state) {
                                    RoomState.UNDISCOVERED -> Color(0, 0, 0, 0)
                                    RoomState.DISCOVERED -> config.colorDiscovered
                                    RoomState.CLEARED -> getSecretGradient(tile)
                                    RoomState.GREEN -> RenderUtils.setColor(Color.BLACK, config.colorTriangleEnd, 255)
                                    RoomState.FAILED -> config.colorFailed
                                }
                                var size = (config.triangleScale * 6f).toDouble()
                                if (tile.state == RoomState.FAILED) size = config.triangleFailedScale * 6.0
                                RenderUtils.renderTriangle(xOffset.toDouble(), yOffset.toDouble(), size, size, color)
                            }
                        } else {
                            getCheckmark(tile.state, config.mapCheckmark)?.let {
                                GlStateManager.enableAlpha()
                                GlStateManager.color(255f, 255f, 255f, 255f)
                                mc.textureManager.bindTexture(it)

                                RenderUtils.drawTexturedQuad(
                                    xOffset + (mapRoomSize - checkmarkSize) / 2,
                                    yOffset + (mapRoomSize - checkmarkSize) / 2,
                                    checkmarkSize,
                                    checkmarkSize
                                )
                                GlStateManager.disableAlpha()
                            }
                        }
                    }

                    val color = if (config.mapColorText) when (tile.state) {
                        RoomState.GREEN -> 0x55ff55
                        RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                        else -> 0xaaaaaa
                    } else 0xffffff

                    var foundSecrets = tile.secretsfound.toString()
                    if (foundSecrets != "0") foundSecrets += "/"
                    else foundSecrets = ""

                    if (config.mapRoomSecrets == 2) {
                        GlStateManager.pushMatrix()
                        GlStateManager.translate(
                            xOffset + (mapRoomSize shr 1).toFloat(), yOffset + 2 + (mapRoomSize shr 1).toFloat(), 0f
                        )
                        GlStateManager.scale(2f, 2f, 1f)
                        RenderUtils.renderCenteredText(listOf(foundSecrets + tile.data.secrets.toString()), 0, 0, color)
                        GlStateManager.popMatrix()
                    }

                    val name = mutableListOf<String>()

                    if (config.mapRoomNames != 0 && tile.data.type.equalsOneOf(
                            RoomType.PUZZLE,
                            RoomType.TRAP
                        ) || config.mapRoomNames == 2 && tile.data.type.equalsOneOf(
                            RoomType.NORMAL, RoomType.RARE, RoomType.CHAMPION
                        )
                    ) {
                        name.addAll(tile.data.name.split(" "))
                    }
                    if (tile.data.type == RoomType.NORMAL && config.mapRoomSecrets == 1) {
                        name.add(foundSecrets + tile.data.secrets.toString())
                    }
                    // Offset + half of roomsize
                    RenderUtils.renderCenteredText(
                        name,
                        xOffset + (mapRoomSize shr 1),
                        yOffset + (mapRoomSize shr 1),
                        color
                    )
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private fun getSecretGradient(tile: Room) : Color {
        if (tile.data.secrets == 0) return RenderUtils.setColor(Color(0,0,0), config.colorTriangleEnd, 255)
        val foundPercentage = ((tile.secretsfound.toFloat() / tile.data.secrets.toFloat()) * 100f).coerceIn(0f, 100f) //some rooms display e.g. 2/1
        val first = min(255f, round(foundPercentage * 2f * 2.55f)).toInt() //first 50%
        val second = 255 - max(0f, round((foundPercentage - 50) * 2f * 2.55f)).toInt() //last 50%
        return RenderUtils.setColor(RenderUtils.setColor(Color(0,0,0), config.colorTriangleStart, second), config.colorTriangleEnd, first)
    }

    private fun getCheckmark(state: RoomState, type: Int): ResourceLocation? {
        return when (type) {
            1 -> when (state) {
                RoomState.CLEARED -> defaultWhite
                RoomState.GREEN -> defaultGreen
                RoomState.FAILED -> defaultCross
                else -> null
            }

            2 -> when (state) {
                RoomState.CLEARED -> neuWhite
                RoomState.GREEN -> neuGreen
                RoomState.FAILED -> neuCross
                else -> null
            }

            else -> null
        }
    }

    private fun renderEntities() {
        if (config.mimicOnMap &&! ScoreCalc.mimicKilled && MimicDetector.mimicPos != null) {
            RenderUtils.drawTextureAtPos(MimicDetector.mimicPos!!, mimicHead, config.mimicHeadScale * 6.0, true)
        }
    }

    private fun renderPlayerHeads() {
        try {
            if (Dungeon.dungeonTeammates.isEmpty()) {
                RenderUtils.drawPlayerHead(mc.thePlayer.name, DungeonPlayer(mc.thePlayer.locationSkin).apply {
                    yaw = mc.thePlayer.rotationYawHead
                })
            } else {
                Dungeon.dungeonTeammates.forEach { (name, teammate) ->
                    if (!teammate.dead) {
                        RenderUtils.drawPlayerHead(name, teammate)
                    }
                }
            }
        } catch (e: ConcurrentModificationException) {
            Utils.modMessage("If you see this, ping me in supporter gen to fix map rendering.")
        }
    }

    private fun drawRoomConnector(
        x: Int,
        y: Int,
        doorWidth: Int,
        doorway: Boolean,
        vertical: Boolean,
        color: Color,
    ) {
        val doorwayOffset = if (mapRoomSize == 16) 5 else 6
        val width = if (doorway) 6 else mapRoomSize
        var x1 = if (vertical) x + mapRoomSize else x
        var y1 = if (vertical) y else y + mapRoomSize
        if (doorway) {
            if (vertical) y1 += doorwayOffset else x1 += doorwayOffset
        }
        RenderUtils.renderRect(
            x1.toDouble(),
            y1.toDouble(),
            (if (vertical) doorWidth else width).toDouble(),
            (if (vertical) width else doorWidth).toDouble(),
            color
        )
    }

    private fun renderRunInformation() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(0f, if (inBoss && config.mapHideInBoss == 1) 2.5f else 128f, 0f)
        GlStateManager.scale(0.66, 0.66, 1.0)
        mc.fontRendererObj.drawString(
            "Secrets: ${RunInformation.secretsFound}/${Dungeon.Info.secretCount}",
            5,
            0,
            0xffffff
        )
        mc.fontRendererObj.drawString((if (RunInformation.cryptsCount > 4) "§a" else "§c") + "Crypts: ${RunInformation.cryptsCount}", 85, 0, 0xffffff)
        mc.fontRendererObj.drawString("Deaths: ${RunInformation.deathCount}", 140, 0, 0xffffff)
        if (config.scoreCalc) {
            if (config.minSecrets) {
                val text = "Needed: ${ScoreCalc.minSecrets}" //56
                mc.fontRendererObj.drawString(text, 42, 15, 0xffffff)
            }
            val scoreColor = if (ScoreCalc.score >= 300 || (dungeonFloor < 5 && ScoreCalc.score >= 270)) 0x55ff55 else 0xffffff
            val text = "Score: ${ScoreCalc.score}" //50
            mc.fontRendererObj.drawString(text, if (config.minSecrets) 105 else 75, 15, scoreColor)
        }
        GlStateManager.popMatrix()
    }
}
