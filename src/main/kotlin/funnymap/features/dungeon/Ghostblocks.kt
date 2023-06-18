package funnymap.features.dungeon

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import funnymap.FunnyMap.Companion.keybindings
import funnymap.FunnyMap.Companion.mc
import funnymap.config.Config
import funnymap.core.GhostblockData
import funnymap.core.map.Direction
import funnymap.core.map.Room
import funnymap.core.map.RoomType
import funnymap.utils.LocationUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockColored
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemPickaxe
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.io.File
import java.io.FileReader

object Ghostblocks {
    val colorableIDs: List<Int> = listOf(35, 95, 159, 160, 171)
    private val colors = listOf(EnumDyeColor.BLACK, EnumDyeColor.BLUE, EnumDyeColor.BROWN, EnumDyeColor.CYAN,
        EnumDyeColor.GRAY, EnumDyeColor.GREEN, EnumDyeColor.LIGHT_BLUE, EnumDyeColor.LIME, EnumDyeColor.MAGENTA, EnumDyeColor.ORANGE,
        EnumDyeColor.PINK, EnumDyeColor.PURPLE, EnumDyeColor.PURPLE, EnumDyeColor.WHITE, EnumDyeColor.YELLOW)
    val illegalBlocks = listOf(Blocks.acacia_door, Blocks.air, Blocks.anvil, Blocks.beacon, Blocks.bed, Blocks.birch_door, Blocks.brewing_stand, Blocks.chest, Blocks.command_block, Blocks.crafting_table,
        Blocks.dark_oak_door, Blocks.daylight_detector, Blocks.daylight_detector_inverted, Blocks.dispenser, Blocks.dropper, Blocks.enchanting_table, Blocks.ender_chest, Blocks.flowing_lava, Blocks.flowing_water,
        Blocks.furnace, Blocks.hopper, Blocks.jungle_door, Blocks.lava, Blocks.lever, Blocks.noteblock, Blocks.oak_door, Blocks.powered_comparator, Blocks.powered_repeater, Blocks.skull, Blocks.standing_sign,
        Blocks.stone_button, Blocks.trapdoor, Blocks.trapped_chest, Blocks.unpowered_comparator, Blocks.unpowered_repeater, Blocks.wall_sign, Blocks.water, Blocks.wooden_button)
    var dir = "config/funnymap/Ghostblocks/"
    val blocks: HashMap<String, HashMap<String, GhostblockData>> = HashMap()
    var oldBlocks: HashMap<String, MutableList<Backup>> = HashMap()
    var restored = mutableListOf<BlockPos>()
    var currentAmount: Int = 0
    var deleteStatus:Room? = null
    var stopRendering = false

    var wasdown = false

    fun loadData() {
        blocks.clear()
        var count = 0
        File(dir).listFiles()?.forEach { category -> category.listFiles()?.forEach { room -> if (room.isFile && room.path.endsWith(".json")) {
            try {
                val cat = blocks[category.nameWithoutExtension] ?: HashMap()
                cat[room.nameWithoutExtension] = Gson().fromJson(FileReader(room), object : TypeToken<GhostblockData>() {}.type)
                blocks[category.nameWithoutExtension] = cat
                count++
            } catch (e: Exception) {
                println("Error reading room ${category.nameWithoutExtension} - ${room.nameWithoutExtension}")
            }
        } } }
        println("Indexed $count files")
    }

    @SubscribeEvent
    fun onKey(event: InputEvent.KeyInputEvent) {
        if (mc.thePlayer == null || !Config.GBToggle ||! Keyboard.isCreated()) return
        if (Keyboard.getEventKeyState()) {
            val kc = Keyboard.getEventKey()
            if (keybindings[0].keyCode == kc && (!Config.GBSingle || Keyboard.isKeyDown(Keyboard.KEY_LMENU))) makeGB(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            if (keybindings[1].keyCode == kc && (!Config.GBSingle || Keyboard.isKeyDown(Keyboard.KEY_LMENU))) makeFakeGB(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            if (keybindings[2].keyCode == kc) deleteRadius()
            if (keybindings[3].keyCode == kc) reload()
            if (keybindings[4].keyCode == kc) unrender()
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Keyboard.isCreated() || mc.thePlayer == null || Keyboard.isKeyDown(Keyboard.KEY_LMENU) || !Config.GBToggle) return
        if (keybindings[0].isKeyDown && Config.GBSingle) makeGB(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        if (keybindings[1].isKeyDown && Config.GBSingle) makeFakeGB(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
        wasdown = if (mc.gameSettings.keyBindAttack.isKeyDown) {
            if (mc.thePlayer.heldItem != null) {
                if (((Config.GBSingle && !Keyboard.isKeyDown(Keyboard.KEY_LMENU)) || (!wasdown && (!Config.GBSingle || Keyboard.isKeyDown(Keyboard.KEY_LMENU)))) &&
                    ((mc.thePlayer.heldItem.item is ItemPickaxe && Config.GBPickaxe) || (mc.thePlayer.heldItem.displayName.lowercase().contains("leap") && Config.GBLeap)))
                    makeGB(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
            }
            true
        } else false
    }

    private fun makeGB(key: Boolean) {
        if (stopRendering || mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.ENTITY)) return
        makeGB(mc.objectMouseOver.blockPos, key)
    }

    private fun makeFakeGB(key: Boolean) {
        if (stopRendering ||! mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) return
        var pos = mc.objectMouseOver.blockPos
        if (key) pos = pos.offset(mc.objectMouseOver.sideHit)
        makeFakeGB(pos)
    }

    fun makeGB(pos: BlockPos, key: Boolean) {
        val old = oldBlocks[getName()]?.find { pos == it.pos }
        if (old?.c == 1) {
            mc.theWorld?.setBlockState(old.pos, old.state)
            oldBlocks[getName()]?.remove(old)
            removeBlock(pos, 1)
        } else {
            if (ghostblock(pos, key) && LocationUtils.inDungeons && old == null) {
                addBlock(pos, 0)
            }
        }
    }

    fun makeFakeGB(pos: BlockPos) {
        val old = oldBlocks[getName()]?.find { pos == it.pos }
        if (old?.c == 0) {
            mc.theWorld?.setBlockState(old.pos, old.state)
            oldBlocks[getName()]?.remove(old)
            removeBlock(pos, 0)
        } else {
            placeblock(pos)
            if (LocationUtils.inDungeons && old == null) {
                addBlock(pos, 1)
            }
        }
    }

    fun deleteRadius() {
        var pos = mc.thePlayer.positionVector
        val re = mutableListOf<Backup>()
        oldBlocks[getName()]?.forEach { block -> if (pos.distanceTo(Vec3(block.pos)) <= Config.GBDelRadius) mc.theWorld?.setBlockState(block.pos, block.state) else re.add(block) }
        oldBlocks[getName()] = re
        if (!LocationUtils.inDungeons || LocationUtils.currentRoom == null) return
        val category = getCategory()
        val name = getName()
        if (category != "NONE" && name != "NONE" && name != "-1") {
            pos = relativeOfActual(pos) ?: return
            val re1 = GhostblockData(mutableSetOf(), mutableSetOf())
            blocks[category]?.get(name)?.placed?.forEach { list -> if (list.size == 3 && pos.distanceTo(Vec3(listToPos(list))) > Config.GBDelRadius) re1.placed?.add(list) }
            blocks[category]?.get(name)?.ghost?.forEach { list -> if (list.size == 3 && pos.distanceTo(Vec3(listToPos(list))) > Config.GBDelRadius) re1.ghost?.add(list) }
            blocks[category]?.put(name, re1)
            writeData(File("$dir${category}/${name}.json"), re1)
        }
    }

    fun render() {
        if (!Config.GBToggle ||! Config.loadGBs ||! LocationUtils.inDungeons || LocationUtils.currentRoom == null || stopRendering) return
        val data = blocks[getCategory()]?.get(getName())?: return
        val reset = Config.cancelPackets && (Config.rightClickReset == 1 || (Config.rightClickReset == 2 && LocationUtils.currentRoom?.data?.type == RoomType.TRAP))
        data.ghost?.forEach { list ->
            if (list.size == 3) {
                val pos = listToPos(list)?.let { actualOfRelative(it) }
                if (pos != null) {
                    if (restored.contains(pos) && reset) {
                        val state = oldBlocks[getName()]?.find { it.pos == pos }?.state
                        if (state != null) mc.theWorld.setBlockState(pos, state)
                    } else ghostblock(BlockPos(pos), true)
                }
            }
        }
        data.placed?.forEach { list ->
            if (list.size == 3) {
                val pos = listToPos(list)?.let { actualOfRelative(it) }
                if (pos != null) {
                    if (restored.contains(pos) && reset) {
                        val state = oldBlocks[getName()]?.find { it.pos == pos }?.state
                        if (state != null) mc.theWorld.setBlockState(pos, state)
                    } else placeblock(BlockPos(pos))
                }
            }
        }
    }

    fun reload() {
        stopRendering = false
        restored.clear()
        oldBlocks[getName()]?.forEach { block -> mc.theWorld?.setBlockState(block.pos, block.state)}
        oldBlocks.remove(getName())
        render()
    }

    fun unrender() {
        stopRendering = true
        restored.clear()
        oldBlocks.forEach { mapping -> mapping.value.forEach { block -> mc.theWorld?.setBlockState(block.pos, block.state)}}
        oldBlocks.clear()
    }


    fun ghostblock(pos: BlockPos, key: Boolean) : Boolean {
        if (!illegalBlocks.contains(mc.theWorld.getBlockState(pos).block) || key) {
            backupBlock(pos, 0)
            mc.theWorld?.setBlockToAir(pos)
            return true
        }
        return false
    }

    fun placeblock(pos: BlockPos) {
        var state = Block.getStateById(Config.GBFakeState)
        if (colorableIDs.contains(Config.GBFakeState)) state =
            state.withProperty(BlockColored.COLOR, colors[Config.GBFakeStateColor])
        backupBlock(pos, 1)
        mc.theWorld?.setBlockState(pos, state)
    }

    fun backupBlock(pos: BlockPos, c: Int) {
        if (!LocationUtils.inBoss || mc.theWorld.getChunkFromChunkCoords(pos.x shr 4, pos.z shr 4).isLoaded) { //to ensure not loaded blocks are not saved, this "never" happens during clear
            val name = getName()
            if (oldBlocks[name]?.toMutableList()?.find { pos == it.pos } == null) {
                if (oldBlocks[name] == null) oldBlocks[name] = mutableListOf()
                oldBlocks[name]?.add(Backup(pos, c))
            }
        }
    }

    fun addBlock(pos: BlockPos, c: Int) {
        if (!LocationUtils.inDungeons || LocationUtils.currentRoom == null || (!Config.GBSave && c == 0)  || (!Config.GBFakeSave && c == 1)) return
        val category = getCategory()
        val name = getName()
        if (category != "NONE" && name != "NONE" && name != "-1") {
            val list = posToList(BlockPos(relativeOfActual(pos) ?: return))
            if (list.size != 3) return
            val re = blocks.getOrDefault(category, HashMap()).getOrDefault(name, GhostblockData(mutableSetOf(), mutableSetOf()))
            if (c == 0) re.ghost?.add(list)
            else re.placed?.add(list)
            val re1 = blocks.getOrDefault(category, HashMap())
            re1[name] = re
            blocks[category] = re1
            writeData(File("$dir${category}/${name}.json"), re)
        }
    }

    fun removeBlock(pos: BlockPos, c: Int) {
        if (!LocationUtils.inDungeons || LocationUtils.currentRoom == null || (!Config.GBSave && c == 0)  || (!Config.GBFakeSave && c == 1)) return
        val category = getCategory()
        val name = getName()
        if (category != "NONE" && name != "NONE" && name != "-1") {
            val list = posToList(BlockPos(relativeOfActual(pos) ?: return))
            if ((if (c == 0) blocks[category]?.get(name)?.ghost?.remove(list)
                else blocks[category]?.get(name)?.placed?.remove(list)) == true) {
                writeData(File("$dir${category}/${name}.json"), blocks[category]?.get(name)!!)
            }
        }
    }

    fun getName() : String {
        val name = LocationUtils.currentRoom?.data?.name ?: "NONE"
        return getName(name)
    }
    fun getName(name: String) : String {
        if (name == "Entrance") return "$name ${((LocationUtils.currentRoom?.data?.cores?.indexOf(LocationUtils.currentRoom?.core)?: 0) + 1)}"
        return name
    }

    fun getCategory() : String {
        return ((LocationUtils.currentRoom?.data?.type) ?: "NONE").toString()
    }

    class Backup (val pos: BlockPos, val c: Int? = null, val state: IBlockState = mc.theWorld.getBlockState(pos).block.getActualState(mc.theWorld.getBlockState(pos), mc.theWorld, pos))

    fun listToPos(list: List<Int>) : BlockPos? {
        if (list.size == 3) {
            return BlockPos(list[0], list[1], list[2])
        }
        return null
    }

    fun posToList(pos: BlockPos) : List<Int> {
        return listOf(pos.x, pos.y, pos.z)
    }

    fun writeData(file: File, data: GhostblockData) : Boolean {
        return try {
            file.parentFile.mkdirs()
            file.writeText(GsonBuilder().setPrettyPrinting().create().toJson(data, object : TypeToken<GhostblockData>() {}.type))
            true
        } catch (e: Exception) {
            //println("Error writing ghostblock data")
            e.printStackTrace()
            false
        }
    }

    fun relativeOfActual(pos: BlockPos) : BlockPos? {
        return BlockPos(relativeOfActual(Vec3(pos)) ?: return null)
    }
    fun relativeOfActual(pos: Vec3) : Vec3? {
        if (LocationUtils.currentRoom == null || LocationUtils.currentRoom?.data?.type == RoomType.BOSS) return pos
        val corner = LocationUtils.currentRoom?.corner ?: return null
        return when (LocationUtils.currentRoom?.direction) {
            Direction.NW -> Vec3(pos.xCoord - corner.x, pos.yCoord, pos.zCoord - corner.y)
            Direction.NE -> Vec3(pos.zCoord - corner.y, pos.yCoord, -(pos.xCoord - corner.x))
            Direction.SE -> Vec3(-(pos.xCoord - corner.x), pos.yCoord, -(pos.zCoord - corner.y))
            Direction.SW -> Vec3(-(pos.zCoord - corner.y), pos.yCoord, pos.xCoord - corner.x)
            else -> null
        }
    }

    fun actualOfRelative(pos: BlockPos) : BlockPos? {
        if (LocationUtils.currentRoom == null || LocationUtils.currentRoom?.data?.type == RoomType.BOSS) return pos
        val corner = LocationUtils.currentRoom?.corner ?: return null
        return when (LocationUtils.currentRoom?.direction) {
            Direction.NW -> BlockPos(pos.x + corner.x, pos.y, pos.z + corner.y)
            Direction.NE -> BlockPos(-(pos.z - corner.x), pos.y, pos.x + corner.y)
            Direction.SE -> BlockPos(-(pos.x - corner.x), pos.y, -(pos.z - corner.y))
            Direction.SW -> BlockPos(pos.z + corner.x, pos.y, -(pos.x - corner.y))
            else -> null
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        oldBlocks = HashMap()
        restored.clear()
    }
}