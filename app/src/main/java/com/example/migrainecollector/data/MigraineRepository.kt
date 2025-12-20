package com.example.migrainecollector.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MigraineRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("migraine_data", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Map of Date -> List of ColorInts
    private val _migraineData = MutableStateFlow<Map<LocalDate, List<Int>>>(loadData())
    val migraineData: StateFlow<Map<LocalDate, List<Int>>> = _migraineData.asStateFlow()

    private val _markerColor = MutableStateFlow<Int>(loadGlobalColor())
    val markerColor: StateFlow<Int> = _markerColor.asStateFlow()

    private fun loadData(): Map<LocalDate, List<Int>> {
        val entriesSet = prefs.getStringSet("migraine_entries", null)
        
        if (entriesSet != null) {
            // Load new format: "YYYY-MM-DD|COLOR1,COLOR2"
            return entriesSet.mapNotNull { entry ->
                try {
                    val parts = entry.split("|")
                    if (parts.size == 2) {
                        val date = LocalDate.parse(parts[0], dateFormatter)
                        val colorStrings = parts[1].split(",")
                        val colors = colorStrings.map { it.toInt() }
                        date to colors
                    } else {
                        // Fallback/Migration for single color int format if mixed?
                        // Assuming new format is strict, but let's handle single int existing
                        null
                    }
                } catch (e: Exception) {
                    // Try parsing as legacy "YYYY-MM-DD|INT" just in case we are reading a file 
                    // that was saved in the previous step (single int)
                    try {
                         val parts = entry.split("|")
                         val date = LocalDate.parse(parts[0], dateFormatter)
                         val color = parts[1].toInt()
                         date to listOf(color)
                    } catch (e2: Exception) {
                        null
                    }
                }
            }.toMap()
        } else {
            // Legacy Migration (from Set<Date>)
            val oldDates = prefs.getStringSet("dates", emptySet()) ?: emptySet()
            val currentColor = loadGlobalColor()
            val migratedMap = oldDates.mapNotNull {
                try {
                    LocalDate.parse(it, dateFormatter) to listOf(currentColor)
                } catch (e: Exception) {
                    null
                }
            }.toMap()
            
            if (migratedMap.isNotEmpty()) {
                saveData(migratedMap)
            }
            return migratedMap
        }
    }

    fun toggleDate(date: LocalDate) {
        val currentMap = _migraineData.value.toMutableMap()
        val currentColors = currentMap[date]?.toMutableList() ?: mutableListOf()
        val selectedColor = _markerColor.value

        if (currentColors.contains(selectedColor)) {
            // Remove this color
            currentColors.remove(selectedColor)
        } else {
            // Add color logic
            if (currentColors.size < 2) {
                currentColors.add(selectedColor)
            } else {
                // Already has 2 colors. Replace the second one (index 1) to allow changing gradient end.
                // Or user might want to replace the first. But typical flow is change 'end'.
                if (currentColors.size >= 2) {
                    currentColors[1] = selectedColor
                }
            }
        }

        if (currentColors.isEmpty()) {
            currentMap.remove(date)
        } else {
            currentMap[date] = currentColors
        }

        _migraineData.value = currentMap
        saveData(currentMap)
    }

    fun saveColor(color: Int) {
        _markerColor.value = color
        prefs.edit {
            putInt("marker_color", color)
        }
    }

    private fun loadGlobalColor(): Int {
        return prefs.getInt("marker_color", 0xFFFF5252.toInt())
    }

    private fun saveData(data: Map<LocalDate, List<Int>>) {
        val stringSet = data.map { (date, colors) ->
            val colorString = colors.joinToString(",")
            "${date.format(dateFormatter)}|$colorString"
        }.toSet()
        
        prefs.edit {
            putStringSet("migraine_entries", stringSet)
        }
    }
}
