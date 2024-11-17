package fm.mrc.rollcount

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fm.mrc.rollcount.data.AttendanceStatus
import fm.mrc.rollcount.ui.theme.RollcountTheme
import fm.mrc.rollcount.viewmodel.AttendanceViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RollcountTheme {
                AttendanceApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceApp() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SmallTopAppBar(
                title = { Text("Attendance Tracker") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        AttendanceContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun AttendanceContent(
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AttendanceStatsCard(
            percentage = viewModel.getAttendancePercentage(),
            presentCount = viewModel.getAttendanceCounts().first,
            absentCount = viewModel.getAttendanceCounts().second
        )
        
        CalendarView(
            viewModel = viewModel,
            selectedDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    selectedDate?.let { date ->
                        viewModel.markAttendance(date, AttendanceStatus.PRESENT)
                        selectedDate = null
                    }
                },
                enabled = selectedDate != null
            ) {
                Text("Present")
            }
            
            Button(
                onClick = {
                    selectedDate?.let { date ->
                        viewModel.markAttendance(date, AttendanceStatus.ABSENT)
                        selectedDate = null
                    }
                },
                enabled = selectedDate != null
            ) {
                Text("Absent")
            }
        }
    }
}

@Composable
fun CalendarView(
    viewModel: AttendanceViewModel,
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val daysInMonth = remember { currentMonth.lengthOfMonth() }
    val firstDayOfMonth = remember { currentMonth.atDay(1).dayOfWeek.value }
    
    Column {
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Weekday headers
            items(7) { dayOfWeek ->
                Text(
                    text = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")[dayOfWeek],
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Empty spaces before first day
            items(firstDayOfMonth - 1) {
                Box(modifier = Modifier.padding(4.dp))
            }
            
            // Days of the month
            items(daysInMonth) { day ->
                val date = currentMonth.atDay(day + 1)
                val epochDay = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                val isSelected = selectedDate == epochDay
                val status = viewModel.attendanceState[epochDay]
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .border(1.dp, Color.LightGray)
                        .background(
                            when (status) {
                                AttendanceStatus.PRESENT -> Color.Green.copy(alpha = 0.3f)
                                AttendanceStatus.ABSENT -> Color.Red.copy(alpha = 0.3f)
                                else -> if (isSelected) Color.Blue.copy(alpha = 0.1f) else Color.Transparent
                            }
                        )
                        .clickable { onDateSelected(epochDay) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = (day + 1).toString())
                }
            }
        }
    }
}

@Composable
fun AttendanceStatsCard(
    percentage: Float,
    presentCount: Int,
    absentCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Attendance Percentage",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "%.1f%%".format(percentage),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Present: $presentCount | Absent: $absentCount",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AttendanceAppPreview() {
    RollcountTheme {
        AttendanceApp()
    }
}