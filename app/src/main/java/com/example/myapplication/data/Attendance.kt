package com.example.myapplication.data

data class Attendance(
    val id: String,
    val studentId: String,
    val activityId: String,
    val date: String,
    val isPresent: Boolean
)

