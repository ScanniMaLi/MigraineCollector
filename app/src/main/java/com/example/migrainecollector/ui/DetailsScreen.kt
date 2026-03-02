package com.example.migrainecollector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.migrainecollector.data.DailyEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DetailsScreen(
        date: LocalDate,
        entry: DailyEntry,
        markerColor: Color,
        onSave: (DailyEntry) -> Unit,
        onBack: () -> Unit,
        onColorSelected: (Color) -> Unit,
        modifier: Modifier = Modifier
) {
    var colors by remember { mutableStateOf(entry.colors) }
    var sleep by remember { mutableStateOf(entry.sleep) }
    var food by remember { mutableStateOf(entry.food) }
    var weather by remember { mutableStateOf(entry.weather) }
    var activity by remember { mutableStateOf(entry.activity) }
    var stress by remember { mutableStateOf(entry.stress) }
    var meds by remember { mutableStateOf(entry.meds) }
    var health by remember { mutableStateOf(entry.health) }
    var misc by remember { mutableStateOf(entry.misc) }

    val scrollState = rememberScrollState()

    Column(
            modifier = modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
                text = "Mark the Date",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))

        ColorPicker(
                selectedColor = markerColor,
                onColorSelected = { color ->
                    onColorSelected(color)
                    val c = color.toArgb()
                    val newColors = colors.toMutableList()
                    if (newColors.contains(c)) {
                        newColors.remove(c)
                    } else {
                        if (newColors.size < 2) {
                            newColors.add(c)
                        } else if (newColors.size >= 2) {
                            newColors[1] = c
                        }
                    }
                    colors = newColors
                }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (colors.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Current markers:")
                Spacer(modifier = Modifier.width(8.dp))
                colors.forEach {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(it)))
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        } else {
            Text(
                    "No colors selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
                value = sleep,
                onValueChange = { sleep = it },
                label = { Text("Sleep") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = food,
                onValueChange = { food = it },
                label = { Text("Food") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = weather,
                onValueChange = { weather = it },
                label = { Text("Weather") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = activity,
                onValueChange = { activity = it },
                label = { Text("Activity") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = stress,
                onValueChange = { stress = it },
                label = { Text("Stress") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = meds,
                onValueChange = { meds = it },
                label = { Text("Meds") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = health,
                onValueChange = { health = it },
                label = { Text("Health") },
                modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
                value = misc,
                onValueChange = { misc = it },
                label = { Text("Miscellaneous") },
                modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedButton(onClick = onBack) { Text("Go Back") }
            Button(
                    onClick = {
                        onSave(
                                DailyEntry(
                                        colors,
                                        sleep,
                                        food,
                                        weather,
                                        activity,
                                        stress,
                                        meds,
                                        health,
                                        misc
                                )
                        )
                    }
            ) { Text("Save") }
        }
    }
}
