package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val students by viewModel.students.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val attendance by viewModel.attendance.collectAsState()
    var selectedActivityId by remember { mutableStateOf("") }
    var showScore by remember { mutableStateOf(false) }
    var scoreText by remember { mutableStateOf("") }

    // Overall average attendance per student (across all activities/dates)
    val studentAverageMap = remember(attendance) {
        if (attendance.isEmpty()) {
            emptyMap<String, Int>()
        } else {
            val groupedByStudent = attendance.groupBy { it.studentId }
            groupedByStudent.mapValues { (_, records) ->
                val total = records.size
                val presentCount = records.count { it.isPresent }
                if (total > 0) (presentCount * 100) / total else 0
            }
        }
    }
    
    // Update selected activity when activities list changes
    LaunchedEffect(activities) {
        if (selectedActivityId.isEmpty() && activities.isNotEmpty()) {
            selectedActivityId = activities.first().id
        }
    }
    
    // Compute attendance map reactively
    val attendanceMap = remember(selectedActivityId, attendance) {
        if (selectedActivityId.isNotEmpty()) {
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            attendance
                .filter { it.activityId == selectedActivityId && it.date == currentDate }
                .associate { it.studentId to it.isPresent }
        } else {
            emptyMap()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (activities.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Select Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        activities.forEach { activity ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedActivityId == activity.id,
                                    onClick = { selectedActivityId = activity.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activity.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
            
            if (activities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No activities available. Add activities first.")
                }
            } else if (students.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No students available")
                }
            } else if (selectedActivityId.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Please select an activity")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(students) { student ->
                            val isPresent = attendanceMap[student.id] ?: false
                            AttendanceItem(
                                student = student,
                                isPresent = isPresent,
                                onToggle = { present ->
                                    if (selectedActivityId.isNotEmpty()) {
                                        viewModel.markAttendance(selectedActivityId, student.id, present)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val totalStudents = students.size
                            val presentCount = students.count { student -> attendanceMap[student.id] == true }
                            val percentage =
                                if (totalStudents > 0) (presentCount * 100) / totalStudents else 0
                            scoreText =
                                "Present: $presentCount / $totalStudents  (Attendance: $percentage%)"
                            showScore = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("Submit")
                    }

                    if (showScore) {
                        Text(
                            text = scoreText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Average attendance of each student",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    students.forEach { student ->
                        val avg = studentAverageMap[student.id] ?: 0
                        Text(
                            text = "${student.name}: $avg%",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(
    student: Student,
    isPresent: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Roll: ${student.rollNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isPresent,
                    onClick = { onToggle(false) },
                    label = { Text("Absent") }
                )
                FilterChip(
                    selected = isPresent,
                    onClick = { onToggle(true) },
                    label = { Text("Present") }
                )
            }
        }
    }
}

