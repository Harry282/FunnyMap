package funnymap.config

import funnymap.FunnyMap
import funnymap.features.dungeon.MoveMapGui
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.*
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
        options = ["None", "Default", "NEU"]
    )
    var mapCheckmark = 1

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
        name = "Force Skyblock",
        type = PropertyType.SWITCH,
        category = "Debug"
    )
    var forceSkyblock = false

    @Property(
        name = "Nanosecond Scan Timer",
        type = PropertyType.SWITCH,
        category = "Debug"
    )
    var nanoScanTime = false

    init {
        initialize()
        setCategoryDescription(
            "Map", "&f&l Funny Map\n&7Big thanks to &lIllegalMap&r&7 by UnclaimedBloom"
        )
    }

    private object CategorySorting : SortingBehavior() {

        private val configCategories = listOf(
            "Map", "Rooms", "Colors", "Debug"
        )

        private val configSubcategories = listOf(
            "Toggle", "Scanning", "Size", "Render"
        )

        override fun getCategoryComparator(): Comparator<in Category> = compareBy { configCategories.indexOf(it.name) }

        override fun getSubcategoryComparator(): Comparator<in Map.Entry<String, List<PropertyData>>> =
            compareBy { configSubcategories.indexOf(it.key) }
    }
}
