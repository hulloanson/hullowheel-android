package com.hulloanson.hullowheel

import android.content.SharedPreferences
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class GamePadElement {
    abstract val position: Position
}

@Serializable
@SerialName("Slider")
data class Slider(override val position: Position, val inverted: Boolean, val autoZero: Boolean) :
        GamePadElement()

@Serializable
@SerialName("Button")
data class Button(override val position: Position, val label: String) :
        GamePadElement()

@Serializable
data class Position(val nthColumn: Int, val nthRow: Int, val width: Int, val height: Int)

@Serializable
data class UIProfile(
        val columnCount: Int,
        val rowCount: Int,
        val gamePadElements: List<GamePadElement>
)

@Serializable
data class UIProfiles(val profiles: HashMap<String, UIProfile>)

const val DEFAULT_TOTAL_COLUMN = 12

const val DEFAULT_TOTAL_ROW = 4

val DEFAULT_PROFILE = UIProfile(DEFAULT_TOTAL_COLUMN, DEFAULT_TOTAL_ROW, listOf(
//    // gas
//    Slider(Position(width = 2, height = 4, nthColumn = DEFAULT_TOTAL_COLUMN - 2, nthRow = 0), inverted = true, autoZero = true),
//    // brake
//    Slider(Position(width = 2, height = 4, nthColumn = DEFAULT_TOTAL_COLUMN - 4, nthRow = 0), inverted = true, autoZero = true),
    Slider(Position(width = 2, height = 4, nthColumn = 0, nthRow = 0), inverted = true, autoZero = true),
    Slider(Position(width = 2, height = 4, nthColumn = 2, nthRow = 0), inverted = true, autoZero = true),
    Slider(Position(width = 2, height = 4, nthColumn = 4, nthRow = 0), inverted = true, autoZero = true),
    Slider(Position(width = 2, height = 2, nthColumn = 6, nthRow = 0), inverted = true, autoZero = true),
    Slider(Position(width = 2, height = 4, nthColumn = 8, nthRow = 0), inverted = true, autoZero = true),
    Slider(Position(width = 2, height = 4, nthColumn = 10, nthRow = 0), inverted = true, autoZero = true),
//    Button(Position(width = 1, height = 1, nthColumn = 0, nthRow = 0), label = "1"),
//    Button(Position(width = 1, height = 1, nthColumn = 5, nthRow = 0), label = "1"),
))

const val PREF_KEY_PROFILES = "profiles"

const val PREF_KEY_CURRENT_PROFILE = "currentProfile"

const val DEFAULT_PROFILE_NAME = "default"

val DEFAULT_PROFILES_STR = Json.encodeToString(UIProfiles(hashMapOf()))

// TODO: test getCurrentProfile
fun getCurrentProfile(pref: SharedPreferences) : UIProfile {
    val profiles = getProfiles(pref)
    val currProfileName = pref.getString(PREF_KEY_CURRENT_PROFILE, null) ?: DEFAULT_PROFILE_NAME
    return profiles.profiles[currProfileName]!!
}

// TODO: test getProfiles
fun getProfiles(pref: SharedPreferences) : UIProfiles {
    val profilesStr = pref.getString(PREF_KEY_PROFILES, null) ?: DEFAULT_PROFILES_STR
    val profiles =  Json.decodeFromString<UIProfiles>(profilesStr)
    profiles.profiles[DEFAULT_PROFILE_NAME] = DEFAULT_PROFILE
    return profiles
}

// TODO: test getProfile
fun getProfile(profiles: UIProfiles, name: String) : UIProfile {
    return profiles.profiles[name]!!
}

