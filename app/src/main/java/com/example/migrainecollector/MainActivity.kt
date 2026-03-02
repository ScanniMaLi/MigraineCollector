package com.example.migrainecollector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.migrainecollector.data.MigraineRepository
import com.example.migrainecollector.ui.CalendarScreen
import com.example.migrainecollector.ui.DetailsScreen
import com.example.migrainecollector.ui.theme.MigraineCollectorTheme
import java.time.LocalDate

sealed class Screen {
    object Calendar : Screen()
    data class Details(val date: LocalDate) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = MigraineRepository(applicationContext)

        setContent {
            MigraineCollectorTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Calendar) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val migraineData by repository.migraineData.collectAsState()
                    val markerColorInt by repository.markerColor.collectAsState()
                    val markerColor = Color(markerColorInt)

                    when (val screen = currentScreen) {
                        is Screen.Calendar -> {
                            CalendarScreen(
                                    migraineData = migraineData,
                                    markerColor = markerColor,
                                    onDayClick = { /* No top level action needed for single tap, handled in CalendarScreen */
                                    },
                                    onDayDoubleTapped = { date ->
                                        currentScreen = Screen.Details(date)
                                    },
                                    onColorSelected = { color ->
                                        repository.saveColor(color.toArgb())
                                    },
                                    modifier = Modifier.padding(innerPadding)
                            )
                        }
                        is Screen.Details -> {
                            BackHandler { currentScreen = Screen.Calendar }
                            DetailsScreen(
                                    date = screen.date,
                                    entry = migraineData[screen.date]
                                                    ?: com.example.migrainecollector.data
                                                            .DailyEntry(
                                                                    colors = emptyList(),
                                                                    sleep = "",
                                                                    food = "",
                                                                    weather = "",
                                                                    activity = "",
                                                                    stress = "",
                                                                    meds = "",
                                                                    health = "",
                                                                    misc = ""
                                                            ),
                                    markerColor = markerColor,
                                    onSave = { updatedEntry ->
                                        repository.updateEntryDetails(screen.date, updatedEntry)
                                        currentScreen = Screen.Calendar
                                    },
                                    onBack = { currentScreen = Screen.Calendar },
                                    onColorSelected = { color ->
                                        repository.saveColor(color.toArgb())
                                    },
                                    modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
