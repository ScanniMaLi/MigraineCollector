package com.example.migrainecollector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    migraineData: Map<LocalDate, List<Int>>,
    markerColor: Color,
    onDayClick: (LocalDate) -> Unit,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    // Statistics logic
    val migrainesThisMonth = migraineData.keys.count { YearMonth.from(it) == currentMonth }
    val migrainesThisYear = migraineData.keys.count { it.year == currentMonth.year }
    
    // Average calculation
    val averagePerMonth = remember(migraineData) {
        if (migraineData.isEmpty()) 0f else {
            val firstDate = migraineData.keys.minOrNull() ?: LocalDate.now()
            val lastDate = migraineData.keys.maxOrNull() ?: LocalDate.now()
            val startMonth = YearMonth.from(firstDate)
            val endMonth = YearMonth.from(lastDate)
            // Determine total months spanned
            val monthsDiff = (endMonth.year - startMonth.year) * 12 + (endMonth.monthValue - startMonth.monthValue) + 1
            migraineData.size.toFloat() / monthsDiff.coerceAtLeast(1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Migraine Collector",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        MonthHeader(
            month = currentMonth,
            onPreviousClick = { currentMonth = currentMonth.minusMonths(1) },
            onNextClick = { currentMonth = currentMonth.plusMonths(1) }
        )

        DaysOfWeekHeader()

        MonthGrid(
            yearMonth = currentMonth,
            migraineData = migraineData,
            onDayClick = onDayClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(label = "This Month", value = migrainesThisMonth.toString())
            StatItem(label = "This Year", value = migrainesThisYear.toString())
            StatItem(label = "Avg/Month", value = String.format("%.1f", averagePerMonth))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Marker Color",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ColorPicker(
            selectedColor = markerColor,
            onColorSelected = onColorSelected
        )
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun ColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFFFF5252), // Red
        Color(0xFFFF9800), // Orange
        Color(0xFFFFEB3B), // Yellow
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFF9C27B0), // Purple
        Color(0xFF000000)  // Black
    )
    
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (color == selectedColor) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else Modifier
                    )
            )
        }
    }
}

@Composable
fun MonthHeader(
    month: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }
        Text(
            text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNextClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.values().forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                    textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MonthGrid(
    yearMonth: YearMonth,
    migraineData: Map<LocalDate, List<Int>>,
    onDayClick: (LocalDate) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value // 1 (Mon) to 7 (Sun)
    
    val emptySlots = firstDayOfMonth - 1
    val totalSlots = emptySlots + daysInMonth

    val rows = (totalSlots + 6) / 7

    Column {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayOfMonth = index - emptySlots + 1
                    
                    if (dayOfMonth in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayOfMonth)
                        val colors = migraineData[date] ?: emptyList()
                        
                        DayCell(
                            date = date,
                            colors = colors,
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    colors: List<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = when {
        colors.isEmpty() -> Modifier.background(Color.Transparent, CircleShape)
        colors.size == 1 -> Modifier.background(Color(colors[0]), CircleShape)
        else -> Modifier.background(
            brush = Brush.linearGradient(
                colors = colors.map { Color(it) },
                start = Offset.Zero,
                end = Offset.Infinite
            ),
            shape = CircleShape
        )
    }

    Box(
        modifier = modifier
            .aspectRatio(1f) // Square cells
            .padding(4.dp)
            .clickable(onClick = onClick)
            .then(backgroundModifier),
        contentAlignment = Alignment.Center
    ) {
        val textColor = if (colors.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurface
        
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor
        )
    }
}
