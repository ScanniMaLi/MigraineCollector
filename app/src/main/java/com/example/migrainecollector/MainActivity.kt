package com.example.migrainecollector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.migrainecollector.data.MigraineRepository
import com.example.migrainecollector.ui.CalendarScreen
import com.example.migrainecollector.ui.theme.MigraineCollectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = MigraineRepository(applicationContext)
        
        setContent {
            MigraineCollectorTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    val migraineDays by repository.migraineDays.collectAsState()
                    val markerColorInt by repository.markerColor.collectAsState()
                    val markerColor = Color(markerColorInt)
                    
                    CalendarScreen(
                        migraineDays = migraineDays,
                        markerColor = markerColor,
                        onDayClick = { date -> repository.toggleDate(date) },
                        onColorSelected = { color -> repository.saveColor(color.toArgb()) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
