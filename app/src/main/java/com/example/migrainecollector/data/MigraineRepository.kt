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

    private val _migraineDays = MutableStateFlow<Set<LocalDate>>(loadDates())
    val migraineDays: StateFlow<Set<LocalDate>> = _migraineDays.asStateFlow()

    private val _markerColor = MutableStateFlow<Int>(loadColor())
    val markerColor: StateFlow<Int> = _markerColor.asStateFlow()

    private fun loadDates(): Set<LocalDate> {
        val stringSet = prefs.getStringSet("dates", emptySet()) ?: emptySet()
        return stringSet.mapNotNull {
            try {
                LocalDate.parse(it, dateFormatter)
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    fun toggleDate(date: LocalDate) {
        val current = _migraineDays.value.toMutableSet()
        if (current.contains(date)) {
            current.remove(date)
        } else {
            current.add(date)
        }
        _migraineDays.value = current
        saveDates(current)
    }

    fun saveColor(color: Int) {
        _markerColor.value = color
        prefs.edit {
            putInt("marker_color", color)
        }
    }

    private fun loadColor(): Int {
        // Default to undefined/red if not saved. Using a default consistent with UI.
        // We will default to Error Container color in UI if this is 0 or check explicitly
        // But it's better to store ARGB. Let's assume passed Int is ARGB.
        // Defaulting to 0xFFB3261E (Standard Red 600ish) conceptually, but cleaner to return a default
        // and let UI decide or picking a concrete default here.
        // Let's use a standard Red equivalent: 0xFFFF5252
        return prefs.getInt("marker_color", 0xFFFF5252.toInt())
    }

    private fun saveDates(dates: Set<LocalDate>) {
        val stringSet = dates.map { it.format(dateFormatter) }.toSet()
        prefs.edit {
            putStringSet("dates", stringSet)
        }
    }
}
