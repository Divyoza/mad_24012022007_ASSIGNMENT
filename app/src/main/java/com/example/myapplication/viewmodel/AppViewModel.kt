package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Activity
import com.example.myapplication.data.Attendance
import com.example.myapplication.data.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel : ViewModel() {
    
    // Sample students
    private val _students = MutableStateFlow<List<Student>>(
        listOf(
            Student("1", "John Doe", "001"),
            Student("2", "Jane Smith", "002"),
            Student("3", "Bob Johnson", "003"),
            Student("4", "Alice Williams", "004"),
            Student("5", "Charlie Brown", "005")
        )
    )
    val students: StateFlow<List<Student>> = _students.asStateFlow()
    
    // Activities
    private val _activities = MutableStateFlow<List<Activity>>(
        listOf(
            Activity("1", "Mathematics", "Algebra and Geometry", getCurrentDate()),
            Activity("2", "Science", "Physics and Chemistry", getCurrentDate()),
            Activity("3", "English", "Literature and Grammar", getCurrentDate())
        )
    )
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()
    
    // Attendance records
    private val _attendance = MutableStateFlow<List<Attendance>>(emptyList())
    val attendance: StateFlow<List<Attendance>> = _attendance.asStateFlow()
    
    fun addActivity(title: String, description: String) {
        viewModelScope.launch {
            val newActivity = Activity(
                id = (_activities.value.size + 1).toString(),
                title = title,
                description = description,
                date = getCurrentDate()
            )
            _activities.value = _activities.value + newActivity
        }
    }
    
    fun markAttendance(activityId: String, studentId: String, isPresent: Boolean) {
        viewModelScope.launch {
            val existingIndex = _attendance.value.indexOfFirst {
                it.activityId == activityId && it.studentId == studentId && it.date == getCurrentDate()
            }
            
            val newAttendance = Attendance(
                id = if (existingIndex >= 0) _attendance.value[existingIndex].id else (_attendance.value.size + 1).toString(),
                studentId = studentId,
                activityId = activityId,
                date = getCurrentDate(),
                isPresent = isPresent
            )
            
            if (existingIndex >= 0) {
                _attendance.value = _attendance.value.toMutableList().apply {
                    set(existingIndex, newAttendance)
                }
            } else {
                _attendance.value = _attendance.value + newAttendance
            }
        }
    }
    
    fun getAttendanceForActivity(activityId: String, date: String = getCurrentDate()): Map<String, Boolean> {
        return _attendance.value
            .filter { it.activityId == activityId && it.date == date }
            .associate { it.studentId to it.isPresent }
    }
    
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

