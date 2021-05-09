package com.hulloanson.hullowheel

import kotlinx.serialization.Serializable

@Serializable
data class GamePadElement(val nthColumn: Int, val nthRow: Int, val width: Int, val height: Int)

@Serializable
data class UIProfile(val columnCount: Int, val rowCount: Int, val gamePadElements: List<GamePadElement>)

@Serializable
data class UIProfiles (val profiles: HashMap<String, UIProfile>)
