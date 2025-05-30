package com.example.studentattendanceapp.ui.screens.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

@Composable
fun StudentCheckinScreen() {
    var sessionCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Check-in to Class",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = sessionCode,
            onValueChange = { 
                sessionCode = it
                error = null
                success = false
            },
            label = { Text("Session Code") },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (sessionCode.isBlank()) {
                    error = "Please enter a session code"
                    return@Button
                }
                isLoading = true
                error = null
                success = false
                markAttendance(sessionCode) { isSuccess, message ->
                    isLoading = false
                    if (isSuccess) {
                        success = true
                        sessionCode = ""
                    } else {
                        error = message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && sessionCode.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Mark Attendance")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (success) {
            Text(
                text = "Attendance marked successfully!",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun markAttendance(sessionCode: String, callback: (Boolean, String?) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(false, "Not authenticated")
        return
    }

    val db = Firebase.firestore

    // First find the active session with this code
    db.collection("attendance_sessions")
        .whereEqualTo("code", sessionCode)
        .whereEqualTo("active", true)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                callback(false, "Invalid or expired session code")
                return@addOnSuccessListener
            }

            val session = documents.documents[0]
            
            // Check if student has already marked attendance
            db.collection("attendance_records")
                .whereEqualTo("sessionId", session.id)
                .whereEqualTo("studentId", currentUser.uid)
                .get()
                .addOnSuccessListener { records ->
                    if (!records.isEmpty) {
                        callback(false, "You have already marked attendance for this session")
                        return@addOnSuccessListener
                    }

                    // Create attendance record
                    val attendanceRecord = hashMapOf(
                        "sessionId" to session.id,
                        "studentId" to currentUser.uid,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("attendance_records")
                        .add(attendanceRecord)
                        .addOnSuccessListener {
                            callback(true, null)
                        }
                        .addOnFailureListener {
                            callback(false, "Failed to mark attendance: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    callback(false, "Failed to check attendance record: ${it.message}")
                }
        }
        .addOnFailureListener {
            callback(false, "Failed to verify session code: ${it.message}")
        }
} 