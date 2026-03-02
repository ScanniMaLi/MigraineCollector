package com.example.migrainecollector.data

import android.content.Context
import androidx.core.content.edit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class DailyEntry(
        val colors: List<Int> = emptyList(),
        val sleep: String = "",
        val food: String = "",
        val weather: String = "",
        val activity: String = "",
        val stress: String = "",
        val meds: String = "",
        val health: String = "",
        val misc: String = ""
) {
    fun toJson(): JSONObject {
        val json = JSONObject()
        val colorArray = JSONArray()
        colors.forEach { colorArray.put(it) }
        json.put("colors", colorArray)
        json.put("sleep", sleep)
        json.put("food", food)
        json.put("weather", weather)
        json.put("activity", activity)
        json.put("stress", stress)
        json.put("meds", meds)
        json.put("health", health)
        json.put("misc", misc)
        return json
    }

    companion object {
        fun fromJson(jsonString: String): DailyEntry? {
            return try {
                val json = JSONObject(jsonString)
                val colors = mutableListOf<Int>()
                if (json.has("colors")) {
                    val arr = json.getJSONArray("colors")
                    for (i in 0 until arr.length()) {
                        colors.add(arr.getInt(i))
                    }
                }
                DailyEntry(
                        colors = colors,
                        sleep = json.optString("sleep", ""),
                        food = json.optString("food", ""),
                        weather = json.optString("weather", ""),
                        activity = json.optString("activity", ""),
                        stress = json.optString("stress", ""),
                        meds = json.optString("meds", ""),
                        health = json.optString("health", ""),
                        misc = json.optString("misc", "")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

class MigraineRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("migraine_data", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _migraineData = MutableStateFlow<Map<LocalDate, DailyEntry>>(loadData())
    val migraineData: StateFlow<Map<LocalDate, DailyEntry>> = _migraineData.asStateFlow()

    private val _markerColor = MutableStateFlow<Int>(loadGlobalColor())
    val markerColor: StateFlow<Int> = _markerColor.asStateFlow()

    private fun loadData(): Map<LocalDate, DailyEntry> {
        val entriesSet = prefs.getStringSet("migraine_entries", null)

        if (entriesSet != null) {
            return entriesSet
                    .mapNotNull { entry ->
                        try {
                            val parts = entry.split("|", limit = 2)
                            if (parts.size == 2) {
                                val date = LocalDate.parse(parts[0], dateFormatter)
                                val dataPart = parts[1]

                                if (dataPart.startsWith("{")) {
                                    // New JSON format
                                    val dailyEntry = DailyEntry.fromJson(dataPart)
                                    if (dailyEntry != null) {
                                        return@mapNotNull date to dailyEntry
                                    }
                                }

                                // Legacy: "COLOR1,COLOR2" or "INT"
                                val colorStrings = dataPart.split(",")
                                val colors = colorStrings.map { it.toInt() }
                                return@mapNotNull date to
                                        DailyEntry(
                                                colors = colors,
                                                sleep = "",
                                                food = "",
                                                weather = "",
                                                activity = "",
                                                stress = "",
                                                meds = "",
                                                health = "",
                                                misc = ""
                                        )
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            // Try parsing as legacy "YYYY-MM-DD|INT" just in case we are reading a
                            // file
                            // that was saved in the previous step (single int)
                            try {
                                val parts = entry.split("|")
                                val date = LocalDate.parse(parts[0], dateFormatter)
                                val color = parts[1].toInt()
                                date to
                                        DailyEntry(
                                                colors = listOf(color),
                                                sleep = "",
                                                food = "",
                                                weather = "",
                                                activity = "",
                                                stress = "",
                                                meds = "",
                                                health = "",
                                                misc = ""
                                        )
                            } catch (e2: Exception) {
                                null
                            }
                        }
                    }
                    .toMap()
        } else {
            // Legacy Migration (from Set<Date>)
            val oldDates = prefs.getStringSet("dates", emptySet()) ?: emptySet()
            val currentColor = loadGlobalColor()
            val migratedMap =
                    oldDates
                            .mapNotNull {
                                try {
                                    LocalDate.parse(it, dateFormatter) to
                                            DailyEntry(
                                                    colors = listOf(currentColor),
                                                    sleep = "",
                                                    food = "",
                                                    weather = "",
                                                    activity = "",
                                                    stress = "",
                                                    meds = "",
                                                    health = "",
                                                    misc = ""
                                            )
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            .toMap()

            if (migratedMap.isNotEmpty()) {
                saveData(migratedMap)
            }
            return migratedMap
        }
    }

    fun toggleDate(date: LocalDate) {
        val currentMap = _migraineData.value.toMutableMap()
        val entry = currentMap[date] ?: DailyEntry()
        val currentColors = entry.colors.toMutableList()
        val selectedColor = _markerColor.value

        if (currentColors.contains(selectedColor)) {
            // Remove this color
            currentColors.remove(selectedColor)
        } else {
            // Add color logic
            if (currentColors.size < 2) {
                currentColors.add(selectedColor)
            } else {
                if (currentColors.size >= 2) {
                    currentColors[1] = selectedColor
                }
            }
        }

        val updatedEntry = entry.copy(colors = currentColors)

        // If everything is empty (no colors, no text), remove it entirely
        if (currentColors.isEmpty() &&
                        updatedEntry.sleep.isEmpty() &&
                        updatedEntry.food.isEmpty() &&
                        updatedEntry.weather.isEmpty() &&
                        updatedEntry.activity.isEmpty() &&
                        updatedEntry.stress.isEmpty() &&
                        updatedEntry.meds.isEmpty() &&
                        updatedEntry.health.isEmpty() &&
                        updatedEntry.misc.isEmpty()
        ) {
            currentMap.remove(date)
        } else {
            currentMap[date] = updatedEntry
        }

        _migraineData.value = currentMap
        saveData(currentMap)
    }

    fun updateEntryDetails(date: LocalDate, updatedEntry: DailyEntry) {
        val currentMap = _migraineData.value.toMutableMap()

        if (updatedEntry.colors.isEmpty() &&
                        updatedEntry.sleep.isEmpty() &&
                        updatedEntry.food.isEmpty() &&
                        updatedEntry.weather.isEmpty() &&
                        updatedEntry.activity.isEmpty() &&
                        updatedEntry.stress.isEmpty() &&
                        updatedEntry.meds.isEmpty() &&
                        updatedEntry.health.isEmpty() &&
                        updatedEntry.misc.isEmpty()
        ) {
            currentMap.remove(date)
        } else {
            currentMap[date] = updatedEntry
        }

        _migraineData.value = currentMap
        saveData(currentMap)
    }

    fun saveColor(color: Int) {
        _markerColor.value = color
        prefs.edit { putInt("marker_color", color) }
    }

    private fun loadGlobalColor(): Int {
        return prefs.getInt("marker_color", 0xFFFF5252.toInt())
    }

    private fun saveData(data: Map<LocalDate, DailyEntry>) {
        val stringSet =
                data
                        .map { (date, entry) ->
                            "${date.format(dateFormatter)}|${entry.toJson().toString()}"
                        }
                        .toSet()

        prefs.edit { putStringSet("migraine_entries", stringSet) }
    }
}
