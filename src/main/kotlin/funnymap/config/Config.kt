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
        name = "Dynamic Rotate",
        type = PropertyType.SWITCH,
        description = "Keeps the entrance room at the bottom. Does not work with rotate map.",
        category = "Map",
        subcategory = "Toggle"
    )
    var mapDynamicRotate = false

    @Property(
        name = "Hide In Boss",
        type = PropertyType.SELECTOR,
        description = "Hides the map in boss.",
        category = "Map",
        subcategory = "Toggle",
        options = ["False", "Only show run information", "Hide all"]
    )
    var mapHideInBoss = 0

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
        name = "Mimic Head",
        type = PropertyType.SWITCH,
        description = "Renders Mimic position on map.",
        category = "Rooms"
    )
    var mimicOnMap = false

    @Property(
        name = "Mimic Head Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "How large the mimic head on map should be.",
        category = "Rooms",
        minF = -0.5f,
        maxF = 2f
    )
    var mimicHeadScale = 1f

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
        name = "Team Info Terminals",
        description = "Shows terminal info and rating with run summary.",
        type = PropertyType.SWITCH,
        category = "Other Features"
    )
    var teamInfoTerminals = false

    @Property(
        name = "Atrocious Limit",
        description = "Lowest, 1st Upper Limit",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = -1f,
        maxF = 100f
    )
    var atrociousThreshold = 7f

    @Property(
        name = "Bad Limit",
        description = "2nd Upper Limit",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = -1f,
        maxF = 100f
    )
    var badThreshold = 10f

    @Property(
        name = "Alright Limit",
        description = "3rd Upper Limit",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = -1f,
        maxF = 100f
    )
    var alrightThreshold = 15f

    @Property(
        name = "Good Limit",
        description = "4th Upper Limit, the next one, the highest is excellent",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = -1f,
        maxF = 100f
    )
    var goodThreshold = 21f

    @Property(
        name = "Device Value",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = 0f,
        maxF = 20f
    )
    var deviceValue = 4.4f

    @Property(
        name = "Terminal Value",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = 0f,
        maxF = 20f
    )
    var terminalValue = 3.7f

    @Property(
        name = "Lever Value",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Terminal Team Info",
        hidden = true,
        minF = 0f,
        maxF = 20f
    )
    var leverValue = 1.05f

    @Property(
        name = "Show Terminal Info",
        description = "Shows team member terminal count at the end of terminal phase.",
        type = PropertyType.SWITCH,
        category = "Other Features"
    )
    var terminalInfo = false

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
        name = "Door ESP",
        description = "Boxes unopened wither/blood doors.",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Door ESP"
    )
    var doorESP = false

    @Property(
        name = "Wither Color",
        type = PropertyType.COLOR,
        category = "Other Features",
        subcategory = "Door ESP",
        allowAlpha = true
    )
    var witherDoorESPColor = Color(0, 0, 0)

    @Property(
        name = "Fairy Color",
        type = PropertyType.COLOR,
        category = "Other Features",
        subcategory = "Door ESP",
        allowAlpha = true
    )
    var fairyDoorESPColor = Color(244, 0, 255)

    @Property(
        name = "Blood Color",
        type = PropertyType.COLOR,
        category = "Other Features",
        subcategory = "Door ESP",
        allowAlpha = true
    )
    var bloodDoorESPColor = Color(255, 0, 0)

    @Property(
        name = "Outline Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Other Features",
        subcategory = "Door ESP"
    )
    var doorOutline = 1f

    @Property(
        name = "Outline Thickness",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Door ESP",
        maxF = 10f
    )
    var doorOutlineThickness = 1f

    @Property(
        name = "Fill Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Other Features",
        subcategory = "Door ESP"
    )
    var doorFill = 0.25f

    @Property(
        name = "Secret waypoints",
        description = "Useful for setting up routes",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var secretWaypoints = false

    @Property(
        name = "Entrance Waypoints",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var entranceWaypoints = false

    @Property(
        name = "Lever Waypoints",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var leverWaypoints = false

    @Property(
        name = "Stonk Waypoints",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var stonkWaypoints = false

    @Property(
        name = "Superboom Waypoints",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var superboomWaypoints = false

    @Property(
        name = "Fairy Soul Waypoints",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var fairySoulWaypoints = false

    @Property(
        name = "Outline Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var secretOutline = 1f

    @Property(
        name = "Outline Thickness",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Secret Waypoints",
        maxF = 5f
    )
    var secretOutlineThickness = 1f

    @Property(
        name = "Fill Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Other Features",
        subcategory = "Secret Waypoints"
    )
    var secretFill = 0.25f

    @Property(
        name = "Show Score Calculation",
        description = "Show Score estimate beneath map",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
    )
    var scoreCalc = true

    @Property(
        name = "Minimum Secrets Required",
        description = "Show Minimum amount of Secrets needed for 300 Score.",
        type = PropertyType.SWITCH,
        category = "Score Calculation",
    )
    var minSecrets = true

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
        addDependency("teamInfoTerminals", "teamInfo")
        addDependency("mimicMessage", "sendMimicFound")
        listOf("witherDoorESPColor", "fairyDoorESPColor", "bloodDoorESPColor", "doorOutline", "doorOutlineThickness", "doorFill").forEach {
            addDependency(it, "doorESP")
        }
        listOf("entranceWaypoints", "leverWaypoints", "stonkWaypoints", "superboomWaypoints", "fairySoulWaypoints", "secretOutline", "secretOutlineThickness", "secretFill").forEach {
            addDependency(it, "secretWaypoints")
        }
        listOf("scoreNotifications", "sendScoreMessages", "lowerScoreMessage", "higherScoreMessage", "sendBothScores").forEach {
            addDependency(it, "scoreMessages")
        }
    }

    private object CategorySorting : SortingBehavior() {

        private val configCategories = listOf(
            "Map", "Rooms", "Colors", "Other Features", "Score Calculation", "Debug"
        )

        private val configSubcategories = listOf(
            "Toggle", "Scanning", "Size", "Render", "Score Messages"
        )

        override fun getCategoryComparator(): Comparator<in Category> = compareBy { configCategories.indexOf(it.name) }

        override fun getSubcategoryComparator(): Comparator<in Map.Entry<String, List<PropertyData>>> =
            compareBy { configSubcategories.indexOf(it.key) }
    }
}
