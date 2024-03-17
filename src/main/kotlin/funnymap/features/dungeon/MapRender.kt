package funnymap.features.dungeon

import funnymap.FunnyMap.mc
import funnymap.config.Config
import funnymap.core.DungeonPlayer
import funnymap.core.map.*
import funnymap.ui.ScoreElement
import funnymap.utils.Location.inBoss
import funnymap.utils.MapUtils
import funnymap.utils.MapUtils.mapRoomSize
import funnymap.utils.RenderUtils
import funnymap.utils.RenderUtils.darken
import funnymap.utils.RenderUtils.grayScale
import funnymap.utils.Utils.equalsOneOf
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

object MapRender {

    private val neuGreen = ResourceLocation("funnymap", "neu/green_check.png")
    private val neuWhite = ResourceLocation("funnymap", "neu/white_check.png")
    private val neuCross = ResourceLocation("funnymap", "neu/cross.png")
    private val defaultGreen = ResourceLocation("funnymap", "default/green_check.png")
    private val defaultWhite = ResourceLocation("funnymap", "default/white_check.png")
    private val defaultCross = ResourceLocation("funnymap", "default/cross.png")

    var dynamicRotation = 0f
    var legitPeek = false

    val legitRender: Boolean
        get() = Config.legitMode && !legitPeek

    fun renderMap() {
        mc.mcProfiler.startSection("border")

        RenderUtils.renderRect(
            0.0, 0.0, 128.0, if (Config.mapShowRunInformation) 142.0 else 128.0, Config.mapBackground
        )

        RenderUtils.renderRectBorder(
            0.0,
            0.0,
            128.0,
            if (Config.mapShowRunInformation) 142.0 else 128.0,
            Config.mapBorderWidth.toDouble(),
            Config.mapBorder
        )

        mc.mcProfiler.endSection()

        if (Config.mapRotate) {
            GlStateManager.pushMatrix()
            setupRotate()
        } else if (Config.mapDynamicRotate) {
            GlStateManager.translate(64.0, 64.0, 0.0)
            GlStateManager.rotate(dynamicRotation, 0f, 0f, 1f)
            GlStateManager.translate(-64.0, -64.0, 0.0)
        }

        mc.mcProfiler.startSection("rooms")
        renderRooms()
        mc.mcProfiler.endStartSection("text")
        renderText()
        if (!inBoss) {
            mc.mcProfiler.endStartSection("heads")
            renderPlayerHeads()
        }
        mc.mcProfiler.endSection()

        if (Config.mapRotate) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
            GlStateManager.popMatrix()
        } else if (Config.mapDynamicRotate) {
            GlStateManager.translate(64.0, 64.0, 0.0)
            GlStateManager.rotate(-dynamicRotation, 0f, 0f, 1f)
            GlStateManager.translate(-64.0, -64.0, 0.0)
        }

        if (Config.mapShowRunInformation) {
            mc.mcProfiler.startSection("footer")
            renderRunInformation()
            mc.mcProfiler.endSection()
        }
    }

    private fun setupRotate() {
        val scale = ScaledResolution(mc).scaleFactor
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(
            (Config.mapX * scale),
            (mc.displayHeight - Config.mapY * scale - 128 * scale * Config.mapScale).toInt(),
            (128 * scale * Config.mapScale).toInt(),
            (128 * scale * Config.mapScale).toInt()
        )
        GlStateManager.translate(64.0, 64.0, 0.0)
        GlStateManager.rotate(-mc.thePlayer.rotationYaw + 180f, 0f, 0f, 1f)

        if (Config.mapCenter) {
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
                if (tile is Unknown) continue
                if (legitRender && tile.state == RoomState.UNDISCOVERED) continue

                val xOffset = (x shr 1) * (mapRoomSize + connectorSize)
                val yOffset = (y shr 1) * (mapRoomSize + connectorSize)

                val xEven = x and 1 == 0
                val yEven = y and 1 == 0

                var color = tile.color

                if (tile.state.equalsOneOf(RoomState.UNDISCOVERED, RoomState.UNOPENED)) {
                    if (Config.mapDarkenUndiscovered) {
                        color = color.darken(1 - Config.mapDarkenPercent)
                    }
                    if (Config.mapGrayUndiscovered && Dungeon.Info.startTime != 0L) {
                        color = color.grayScale()
                    }
                }

                when {
                    xEven && yEven -> if (tile is Room) {
                        RenderUtils.renderRect(
                            xOffset,
                            yOffset,
                            mapRoomSize,
                            mapRoomSize,
                            color
                        )
                    }

                    !xEven && !yEven -> {
                        RenderUtils.renderRect(
                            xOffset,
                            yOffset,
                            (mapRoomSize + connectorSize),
                            (mapRoomSize + connectorSize),
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
        val checkmarkSize = when (Config.mapCheckmark) {
            1 -> 8.0 // default
            else -> 10.0 // neu
        }

        Dungeon.Info.uniqueRooms.forEach { unique ->
            val room = unique.mainRoom
            if (legitRender && room.state.equalsOneOf(RoomState.UNDISCOVERED, RoomState.UNOPENED)) return@forEach
            val checkPos = unique.getCheckmarkPosition()
            val namePos = unique.getNamePosition()
            val xOffsetCheck = (checkPos.first / 2f) * (mapRoomSize + connectorSize)
            val yOffsetCheck = (checkPos.second / 2f) * (mapRoomSize + connectorSize)
            val xOffsetName = (namePos.first / 2f) * (mapRoomSize + connectorSize)
            val yOffsetName = (namePos.second / 2f) * (mapRoomSize + connectorSize)

            if (Config.mapCheckmark != 0 && Config.mapRoomSecrets != 2) {
                getCheckmark(room.state, Config.mapCheckmark)?.let {

                    GlStateManager.enableAlpha()
                    GlStateManager.color(255f, 255f, 255f, 255f)
                    mc.textureManager.bindTexture(it)

                    RenderUtils.drawTexturedQuad(
                        xOffsetCheck + (mapRoomSize - checkmarkSize) / 2,
                        yOffsetCheck + (mapRoomSize - checkmarkSize) / 2,
                        checkmarkSize,
                        checkmarkSize
                    )
                    GlStateManager.disableAlpha()
                }
            }

            val color = if (Config.mapColorText) when (room.state) {
                RoomState.GREEN -> 0x55ff55
                RoomState.CLEARED, RoomState.FAILED -> 0xffffff
                else -> 0xaaaaaa
            } else 0xffffff

            if (Config.mapRoomSecrets == 2) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    xOffsetCheck + (mapRoomSize shr 1), yOffsetCheck + 2 + (mapRoomSize shr 1), 0f
                )
                GlStateManager.scale(2f, 2f, 1f)
                RenderUtils.renderCenteredText(listOf(room.data.secrets.toString()), 0, 0, color)
                GlStateManager.popMatrix()
            }

            val name = mutableListOf<String>()

            if (Config.mapRoomNames != 0 && room.data.type.equalsOneOf(
                    RoomType.PUZZLE,
                    RoomType.TRAP
                ) || Config.mapRoomNames == 2 && room.data.type.equalsOneOf(
                    RoomType.NORMAL, RoomType.RARE, RoomType.CHAMPION
                )
            ) {
                name.addAll(room.data.name.split(" "))
            }
            if (room.data.type == RoomType.NORMAL && Config.mapRoomSecrets == 1) {
                name.add(room.data.secrets.toString())
            }
            // Offset + half of roomsize
            RenderUtils.renderCenteredText(
                name,
                xOffsetName.toInt() + (mapRoomSize shr 1),
                yOffsetName.toInt() + (mapRoomSize shr 1),
                color
            )
        }
        GlStateManager.popMatrix()
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

    private fun renderPlayerHeads() {
        try {
            if (Dungeon.dungeonTeammates.isEmpty()) {
                RenderUtils.drawPlayerHead(mc.thePlayer.name, DungeonPlayer(mc.thePlayer.locationSkin).apply {
                    yaw = mc.thePlayer.rotationYaw
                })
            } else {
                Dungeon.dungeonTeammates.forEach { (name, teammate) ->
                    if (!teammate.dead) {
                        RenderUtils.drawPlayerHead(name, teammate)
                    }
                }
            }
        } catch (_: ConcurrentModificationException) {
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
            x1,
            y1,
            if (vertical) doorWidth else width,
            if (vertical) width else doorWidth,
            color
        )
    }

    private fun renderRunInformation() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(64f, 128f, 0f)
        GlStateManager.scale(2.0 / 3.0, 2.0 / 3.0, 1.0)
        val lines = ScoreElement.runInformationLines()

        val lineOne = lines.takeWhile { it != "split" }.joinToString(separator = "    ")
        val lineTwo = lines.takeLastWhile { it != "split" }.joinToString(separator = "    ")

        mc.fontRendererObj.drawString(lineOne, -mc.fontRendererObj.getStringWidth(lineOne) / 2f, 0f, 0xffffff, true)
        mc.fontRendererObj.drawString(lineTwo, -mc.fontRendererObj.getStringWidth(lineTwo) / 2f, 9f, 0xffffff, true)

        GlStateManager.popMatrix()
    }
}
