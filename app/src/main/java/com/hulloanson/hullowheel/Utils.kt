package com.hulloanson.hullowheel

import android.content.SharedPreferences
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.regex.Matcher
import java.util.regex.Pattern

const val addressPatternRegex = "^(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d{1,5})$"
val addressPattern: Pattern = Pattern.compile(addressPatternRegex)

fun countGroup(matcher: Matcher) {
    matcher.matches()
    matcher.groupCount()
}

fun getAddressPart(address: String, which: Int): String {
    val m = addressPattern.matcher(address)
    countGroup(m)
    return m.group(which + 1)
}

fun parseHost(address: String): String {
    return getAddressPart(address, 0)
}

fun parsePort(address: String): Int {
    return getAddressPart(address, 1).toInt()
}

fun matches(address: String): Boolean {
    return addressPattern.matcher(address).matches()
}

fun getProfiles(pref: SharedPreferences): UIProfiles {
    val prefStr = pref.getString("profiles", null) ?: return UIProfiles(HashMap())
    return Json.decodeFromString(prefStr)
}

fun getCurrentProfileName(pref: SharedPreferences) {
    // TODO: implement
}

fun generateDefaultProfile() {
    // TODO: implement
}