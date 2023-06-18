package funnymap.config

import funnymap.FunnyMap
import funnymap.features.dungeon.Ghostblocks
import funnymap.features.dungeon.MoveMapGui
import funnymap.utils.LocationUtils
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.*
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiControls
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.client.FMLClientHandler
import java.awt.Color
import java.io.File

object Config : Vigilant(File("./config/funnymap/config.toml"), "Funny Map", sortingBehavior = CategorySorting) {

    @Property(
        name = "Map Enabled",
        type = PropertyType.SWITCH,
        description = "Render the map!",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapEnabled = true

    @Property(
        name = "Rotate Map",
        type = PropertyType.SWITCH,
        description = "Rotates map to follow the player.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapRotate = false

    @Property(
        name = "Center Map",
        type = PropertyType.SWITCH,
        description = "Centers the map on the player if Rotate Map is enabled.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapCenter = false

    @Property(
        name = "Hide In Boss",
        type = PropertyType.SWITCH,
        description = "Hides the map in boss.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapHideInBoss = false

    @Property(
        name = "Show Run Information",
        type = PropertyType.SWITCH,
        description = "Shows run information under map.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapShowRunInformation = false

    @Property(
        name = "Show Player Names",
        type = PropertyType.SELECTOR,
        description = "Show player name under player head",
        category = "Map",
        subcategory = "Toggle",
        options = ["Off", "Holding Leap", "Always"]
    )
    var playerHeads = 0

    @Property(
        name = "Auto Scan",
        type = PropertyType.SWITCH,
        description = "Automatically scans when entering dungeon. Manual scan can be done with \"/fmap scan\"",
        category = "Map",
        subcategory = "Scanning"
    )
    var autoScan = true

    @Property(
        name = "Chat Info",
        type = PropertyType.SWITCH,
        description = "Show dungeon overview information after scanning.",
        category = "Map",
        subcategory = "Scanning"
    )
    var scanChatInfo = true

    @Property(
        name = "Map Position",
        type = PropertyType.BUTTON,
        category = "Map",
        subcategory = "Size",
        placeholder = "Edit"
    )
    fun openMoveMapGui() {
        FunnyMap.display = MoveMapGui()
    }

    @Property(
        name = "Reset Map Position",
        type = PropertyType.BUTTON,
        category = "Map",
        subcategory = "Size",
        placeholder = "Reset"
    )
    fun resetMapLocation() {
        mapX = 10
        mapY = 10
    }

    @Property(
        name = "Map X",
        type = PropertyType.NUMBER,
        category = "Map",
        subcategory = "Size",
        hidden = true
    )
    var mapX = 10

    @Property(
        name = "Map Y",
        type = PropertyType.NUMBER,
        category = "Map",
        subcategory = "Size",
        hidden = true
    )
    var mapY = 10

    @Property(
        name = "Map Size",
        type = PropertyType.DECIMAL_SLIDER,
        description = "Scale of entire map.",
        category = "Map",
        subcategory = "Size",
        minF = 0.1f,
        maxF = 4f,
        decimalPlaces = 2
    )
    var mapScale = 1.25f

    @Property(
        name = "Map Text Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "Scale of room names and secret counts relative to map size.",
        category = "Map",
        subcategory = "Size",
        maxF = 2f,
        decimalPlaces = 2
    )
    var textScale = 0.75f

    @Property(
        name = "Player Heads Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "Scale of player heads relative to map size.",
        category = "Map",
        subcategory = "Size",
        maxF = 2f,
        decimalPlaces = 2
    )
    var playerHeadScale = 1f

    @Property(
        name = "Map Background Color",
        type = PropertyType.COLOR,
        category = "Map",
        subcategory = "Render",
        allowAlpha = true
    )
    var mapBackground = Color(0, 0, 0, 100)

    @Property(
        name = "Map Border Color",
        type = PropertyType.COLOR,
        category = "Map",
        subcategory = "Render",
        allowAlpha = true
    )
    var mapBorder = Color(0, 0, 0, 255)

    @Property(
        name = "Border Thickness",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Map",
        subcategory = "Render",
        maxF = 10f
    )
    var mapBorderWidth = 3f

    @Property(
        name = "Dark Undiscovered Rooms",
        type = PropertyType.SWITCH,
        description = "Darkens unentered rooms.",
        category = "Rooms"
    )
    var mapDarkenUndiscovered = true

    @Property(
        name = "Room Names",
        type = PropertyType.SELECTOR,
        description = "Shows names of rooms on map.",
        category = "Rooms",
        options = ["None", "Puzzles / Trap", "All"]
    )
    var mapRoomNames = 1

    @Property(
        name = "Room Secrets",
        type = PropertyType.SELECTOR,
        description = "Shows total secrets of rooms on map.",
        category = "Rooms",
        options = ["Off", "On", "Replace Checkmark"]
    )
    var mapRoomSecrets = 0

    @Property(
        name = "Color Text",
        type = PropertyType.SWITCH,
        description = "Colors name and secret count based on room state.",
        category = "Rooms"
    )
    var mapColorText = false

    @Property(
        name = "Room Checkmarks",
        type = PropertyType.SELECTOR,
        description = "Adds room checkmarks based on room state.",
        category = "Rooms",
        options = ["None", "Default", "NEU", "Triangle"]
    )
    var mapCheckmark = 1

    @Property(
        name = "Triangle Checkmark Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "How large the triangle room state indicator should be.",
        category = "Rooms",
        minF = -0.5f,
        maxF = 2f
    )
    var triangleScale = 1f

    @Property(
        name = "Room Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Colors"
    )
    var mapRoomTransparency = 1f

    @Property(
        name = "Darken Multiplier",
        type = PropertyType.PERCENT_SLIDER,
        description = "How much to darken undiscovered rooms",
        category = "Colors"
    )
    var mapDarkenPercent = 0.4f

    @Property(
        name = "Blood Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorBloodDoor = Color(231, 0, 0)

    @Property(
        name = "Entrance Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorEntranceDoor = Color(20, 133, 0)

    @Property(
        name = "Normal Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorRoomDoor = Color(92, 52, 14)

    @Property(
        name = "Wither Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorWitherDoor = Color(0, 0, 0)

    @Property(
        name = "Opened Wither Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true
    )
    var colorOpenWitherDoor = Color(92, 52, 14)

    @Property(
        name = "Blood Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorBlood = Color(255, 0, 0)

    @Property(
        name = "Entrance Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorEntrance = Color(20, 133, 0)

    @Property(
        name = "Fairy Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorFairy = Color(224, 0, 255)

    @Property(
        name = "Miniboss Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorMiniboss = Color(254, 223, 0)

    @Property(
        name = "Normal Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorRoom = Color(107, 58, 17)

    @Property(
        name = "Mimic Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorRoomMimic = Color(186, 66, 52)

    @Property(
        name = "Puzzle Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorPuzzle = Color(117, 0, 133)

    @Property(
        name = "Rare Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorRare = Color(255, 203, 89)

    @Property(
        name = "Trap Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true
    )
    var colorTrap = Color(216, 127, 51)

    @Property(
        name = "Triangle Discovered Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms"
    )
    var colorDiscovered = Color(0, 0, 0)

    @Property(
        name = "Triangle Failed Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms"
    )
    var colorFailed = Color(60, 60, 60)

    @Property(
        name = "Triangle Scale of Failed Rooms",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Colors",
        subcategory = "Rooms",
        minF =  1f,
        maxF = 2.7f
    )
    var triangleFailedScale = 1f

    @Property(
        name = "Triangle Secret Start Gradient",
        type = PropertyType.SELECTOR,
        description = "At what color the secret gradient will start.",
        category = "Colors",
        subcategory = "Rooms",
        options = ["red", "green", "blue"]
    )
    var colorTriangleStart = 0

    @Property(
        name = "Triangle Secret End Gradient",
        type = PropertyType.SELECTOR,
        description = "At what color the secrets found gradient will end and the color of completed rooms.",
        category = "Colors",
        subcategory = "Rooms",
        options = ["red", "green", "blue"]
    )
    var colorTriangleEnd = 1

    @Property(
        name = "Hypixel API Key",
        type = PropertyType.TEXT,
        category = "Other Features",
        protectedText = true
    )
    var apiKey = ""

    @Property(
        name = "Small Titles",
        description = "If e.g. score titles should be small.",
        type = PropertyType.SWITCH,
        category = "Other Features",
    )
    var smallTitles = false

    @Property(
        name = "Show Team Info",
        description = "Shows team member secrets and room times at end of run. Requires a valid API key.",
        type = PropertyType.SWITCH,
        category = "Other Features"
    )
    var teamInfo = false

    @Property(
        name = "Send Mimic Found",
        description = "Sends a message to party chat when mimic is found.",
        type = PropertyType.SWITCH,
        category = "Other Features"
    )
    var sendMimicFound = false

    @Property(
        name = "Mimic Message",
        description = "Message to be sent when mimic is found.",
        type = PropertyType.TEXT,
        category = "Other Features"
    )
    var mimicMessage = "Mimic Killed!"

    @Property(
        name = "Show Score Calculation",
        description = "Show Score estimate beneath map",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
    )
    var scoreCalc = true

    @Property(
        name = "Minimum Secrets Required",
        description = "Show Minimum amount of Secrets needed for score goal.",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
    )
    var minSecrets = true

    @Property(
        name = "Deaths",
        description = "Display Score lost through deaths",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
    )
    var showDeaths = true

    @Property(
        name = "Score Messages",
        description = "Display Notifications for 270/300 Score.",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
        subcategory = "Score Messages"
    )
    var scoreMessages = true

    @Property(
        name = "Score Notifications",
        description = "How you will be notified for 270/300 score.",
        type = PropertyType.SELECTOR,
        category = "Score Calculation",
        subcategory = "Score Messages",
        options = ["None", "Title", "Chat message", "Both"]
    )
    var scoreNotifications = 1

    @Property(
        name = "Send Score Messages",
        description = "Send score reached messages to party chat.",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
        subcategory = "Score Messages"
    )
    var sendScoreMessages = false

    @Property(
        name = "270 Score Message",
        type = PropertyType.TEXT,
        category = "Score Calculation",
        subcategory = "Score Messages"
    )
    var lowerScoreMessage = "&b270 Score Reached!"

    @Property(
        name = "300 Score Message",
        type = PropertyType.TEXT,
        category = "Score Calculation",
        subcategory = "Score Messages"

    )
    var higherScoreMessage = "&b300 Score Reached!"

    @Property(
        name = "Send Both Messages",
        description = "By default only the max score of the floor will be sent.",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
        subcategory = "Score Messages"

    )
    var sendBothScores = false

    @Property(
        name = "Ghostblock toggle",
        description = "Toggle all ghostblock features. Hold LCTRL -> ghost: override interactables / fake: place block in front of you",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var GBToggle = false

    @Property(
        name = "Keybindings",
        description = "Open Minecraft keybindings gui.",
        type = PropertyType.BUTTON,
        category = "Ghostblocks",
        placeholder = "View"
    )
    fun onClick() {
        Thread{FunnyMap.mc.addScheduledTask{FunnyMap.mc.displayGuiScreen(GuiControls(this.gui(), FunnyMap.mc.gameSettings))}}.start()
    }

    @Property(
        name = "Single Ghostblock",
        description = "Create a single // multiple ghostblocks - hold LALT -> always single",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var GBSingle = true

    @Property(
        name = "Ghostblock Pickaxe",
        description = "Create ghostblocks when using a pickaxe.",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var GBPickaxe = false

    @Property(
        name = "Ghostblock Leap",
        description = "Create ghostblocks when holding a leap.",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var GBLeap = false

    @Property(
        name = "Fake Block",
        description = "Block State of fake blocks.",
        type = PropertyType.SELECTOR,
        category = "Ghostblocks",
        options = ["Air","Stone","Grass Block","Dirt","Cobblestone","Wood Planks","Saplings","Bedrock","Water",
            "Stationary water","Lava","Stationary lava","Sand","Gravel","Gold Ore","Iron Ore","Coal Ore","Wood",
            "Leaves","Sponge","Glass","Lapis Lazuli Ore","Lapis Lazuli Block","Dispenser","Sandstone","Note Block",
            "Bed","Powered Rail","Detector Rail","Sticky Piston","Cobweb","Grass","Dead Bush","Piston","Piston Extension",
            "Wool","Block moved by Piston","Dandelion","Poppy","Brown Mushroom","Red Mushroom","Block of Gold",
            "Block of Iron","Double Stone Slab","Stone Slab","Bricks","TNT","Bookshelf","Moss Stone","Obsidian",
            "Torch","Fire","Monster Spawner","Oak Wood Stairs","Chest","Redstone Wire","Diamond Ore","Block of Diamond",
            "Crafting Table","Wheat","Farmland","Furnace","Burning Furnace","Sign Post","Wooden Door","Ladders","Rail",
            "Cobblestone Stairs","Wall Sign","Lever","Stone Pressure Plate","Iron Door","Wooden Pressure Plate",
            "Redstone Ore","Glowing Redstone Ore","Redstone Torch (inactive)","Redstone Torch (active)","Stone Button",
            "Snow","Ice","Snow","Cactus","Clay","Sugar Cane","Jukebox","Fence","Pumpkin","Netherrack","Soul Sand",
            "Glowstone","Nether Portal","Jack 'o' Lantern","Cake Block","Redstone Repeater (inactive)",
            "Redstone Repeater (active)","Stained Glass","Trapdoor","Monster Egg","Stone Bricks","Huge Brown Mushroom",
            "Huge Red Mushroom","Iron Bars","Glass Pane","Melon","Pumpkin Stem","Melon Stem","Vines","Fence Gate","Brick Stairs",
            "Stone Brick Stairs","Mycelium","Lily Pad","Nether Brick","Nether Brick Fence","Nether Brick Stairs","Nether Wart",
            "Enchantment Table","Brewing Stand","Cauldron","End Portal","End Portal Block","End Stone","Dragon Egg",
            "Redstone Lamp (inactive)","Redstone Lamp (active)","Wooden Double Slab","Wooden Slab","Cocoa","Sandstone Stairs",
            "Emerald Ore","Ender Chest","Tripwire Hook","Tripwire","Block of Emerald","Spruce Wood Stairs","Birch Wood Stairs",
            "Jungle Wood Stairs","Command Block","Beacon","Cobblestone Wall","Flower Pot","Carrots","Potatoes","Wooden Button",
            "Mob Head","Anvil","Trapped Chest","Weighted Pressure Plate (Light)","Weighted Pressure Plate (Heavy)",
            "Redstone Comparator (both states)","Redstone Comparator (both states)","Daylight Sensor","Block of Redstone",
            "Nether Quartz Ore","Hopper","Block of Quartz","Quartz Stairs","Activator Rail","Dropper","Stained Clay",
            "Stained Glass Pane","(Acacia/Dark Oak)","(Acacia/Dark Oak)","Acacia Wood Stairs","Dark Oak Wood Stairs","Slime Block",
            "Barrier","Iron Trapdoor","Prismarine","Lantern","Hay Block","Carpet","Hardened Clay","Block of Coal","Packed Ice","Large Flowers"]
    )
    var GBFakeState = 95

    @Property(
        name = "Fake Block Color",
        description = "Color of certain fake blocks.",
        type = PropertyType.SELECTOR,
        category = "Ghostblocks",
        options = ["black", "blue", "brown", "cyan", "gray", "green", "light blue", "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"],
    )
    var GBFakeStateColor = 4

    @Property(
        name = "Save Ghostblocks",
        description = "Saves ghostblocks to file.",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var GBSave = true

    @Property(
        name = "Save Fake Blocks",
        description = "Saves fake blocks to file.",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var GBFakeSave = true


    @Property(
        name = "Load Ghostblocks",
        description = "Load saved ghostblocks from files.",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var loadGBs = true

    @Property(
        name = "Cancel Packets",
        description = "Cancels packets that would override ghostblocks. May be performance intensive.",
        type = PropertyType.SWITCH,
        category = "Ghostblocks"
    )
    var cancelPackets = true

    @Property(
        name = "Cancel Packets Chunk Range",
        description = "Range in chunks to the player where chunk updates will be overriden in boss.",
        type = PropertyType.SLIDER,
        category = "Ghostblocks",
        hidden = true
    )
    var cancelPacketsRange = 2

    @Property(
        name = "Right click restore",
        description = "Right click on blocks to 'restore' them, if cancel packets is enabled",
        type = PropertyType.SELECTOR,
        category = "Ghostblocks",
        options = ["Off", "On", "In Trap"]
    )
    var rightClickReset = 0

    @Property(
        name = "Deletion Radius",
        description = "Withing what radius of the player saved blocks will be deleted on keybinding use.",
        type = PropertyType.SLIDER,
        category = "Ghostblocks",
        min = 0,
        max = 30
    )
    var GBDelRadius = 5

    @Property(
        name = "Delete ghostblocks",
        description = "Delete all ghostblocks in this room.",
        type = PropertyType.BUTTON,
        category = "Ghostblocks",
        placeholder = "Delete"
    )
    fun deleteRoom() {
        Ghostblocks.deleteStatus = LocationUtils.currentRoom ?: return
        val yes = ChatComponentText("§4[YES]")
        yes.setChatStyle(yes.chatStyle.setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fmap delete yes"))
            .setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§4confirm"))))
        val no = ChatComponentText("§2[NO]")
        no.setChatStyle(no.chatStyle.setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fmap delete no"))
            .setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§2cancel"))))

        FunnyMap.mc.thePlayer.addChatMessage(ChatComponentText("${FunnyMap.CHAT_PREFIX} Delete all ${Ghostblocks.currentAmount} ghostblocks in this room: ${Ghostblocks.deleteStatus?.data?.name}\n").appendSibling(yes).appendSibling(ChatComponentText("   ")).appendSibling(no))
        FMLClientHandler.instance().client.displayGuiScreen(GuiChat())
    }

    @Property(
        name = "Force Skyblock",
        type = PropertyType.SWITCH,
        category = "Debug"
    )
    var forceSkyblock = false

    init {
        initialize()
        setCategoryDescription(
            "Map", "&f&l Funny Map\n&7Big thanks to &lIllegalMap&r&7 by UnclaimedBloom"
        )
        addDependency("mimicMessage", "sendMimicFound")
        listOf("scoreNotifications", "sendScoreMessages", "lowerScoreMessage", "higherScoreMessage", "sendBothScores").forEach { addDependency(it, "scoreMessages") }

        addDependency("rightClickReset", "cancelPackets")
        //todo: !Ghostblocks.colorableIDs.contains(GBFakeState) -> hide GBFakeStateColor
    }

    private object CategorySorting : SortingBehavior() {

        private val configCategories = listOf(
            "Map", "Rooms", "Colors", "Other Features", "Score Calculation", "Ghostblocks", "Debug"
        )

        private val configSubcategories = listOf(
            "Toggle", "Scanning", "Size", "Render", "Score Messages"
        )

        override fun getCategoryComparator(): Comparator<in Category> = compareBy { configCategories.indexOf(it.name) }

        override fun getSubcategoryComparator(): Comparator<in Map.Entry<String, List<PropertyData>>> =
            compareBy { configSubcategories.indexOf(it.key) }
    }
}
