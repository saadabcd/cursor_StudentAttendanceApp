package com.example.studentattendanceapp.ui.screens.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

data class AttendanceRecord(
    val id: String = "",
    val sessionId: String = "",
    val studentId: String = "",
    val timestamp: Long = 0,
    val courseName: String = "",
    val studentName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceHistoryScreen(navController: NavController) {
    var records by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Load attendance records
    LaunchedEffect(Unit) {
        loadAttendanceHistory { result, errorMessage ->
            isLoading = false
            if (result != null) {
                records = result
            } else {
                error = errorMessage
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else if (records.isEmpty()) {
                Text(
                    text = "No attendance records found",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records) { record ->
                        AttendanceRecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(record: AttendanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = record.courseName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (record.studentName.isNotBlank()) {
                Text(
                    text = "Student: ${record.studentName}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "Date: ${formatDate(record.timestamp)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun loadAttendanceHistory(callback: (List<AttendanceRecord>?, String?) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(null, "Not authenticated")
        return
    }

    val db = Firebase.firestore
    val userRole = mutableStateOf<String?>(null)

    // First get the user's role
    db.collection("users")
        .document(currentUser.uid)
        .get()
        .addOnSuccessListener { document ->
            val role = document.getString("role")
            if (role != null) {
                userRole.value = role
                when (role.uppercase()) {
                    "PROFESSOR" -> loadProfessorHistory(currentUser.uid, callback)
                    "STUDENT" -> loadStudentHistory(currentUser.uid, callback)
                    else -> callback(null, "Invalid user role")
                }
            } else {
                callback(null, "User role not found")
            }
        }
        .addOnFailureListener {
            callback(null, "Failed to load user role: ${it.message}")
        }
}

private fun loadProfessorHistory(professorId: String, callback: (List<AttendanceRecord>?, String?) -> Unit) {
    val db = Firebase.firestore
    
    // First get all sessions for this professor
    db.collection("attendance_sessions")
        .whereEqualTo("professorId", professorId)
        .get()
        .addOnSuccessListener { sessions ->
            if (sessions.isEmpty) {
                callback(emptyList(), null)
                return@addOnSuccessListener
            }

            // Create a map of session IDs to course names
            val sessionMap = sessions.documents.associate { 
                it.id to it.getString("courseName").orEmpty()
            }

            // Get all attendance records for these sessions
            db.collection("attendance_records")
                .whereIn("sessionId", sessionMap.keys.toList())
                .get()
                .addOnSuccessListener { records ->
                    // For each record, we need to get the student's name
                    val studentIds = records.documents.map { it.getString("studentId").orEmpty() }.distinct()
                    
                    // Get all student names in one batch
                    db.collection("users")
                        .whereIn("__name__", studentIds)
                        .get()
                        .addOnSuccessListener { students ->
                            val studentNames = students.documents.associate {
                                it.id to it.getString("name").orEmpty()
                            }

                            val attendanceRecords = records.documents.map { doc ->
                                val sessionId = doc.getString("sessionId").orEmpty()
                                val studentId = doc.getString("studentId").orEmpty()
                                AttendanceRecord(
                                    id = doc.id,
                                    sessionId = sessionId,
                                    studentId = studentId,
                                    timestamp = doc.getLong("timestamp") ?: 0,
                                    courseName = sessionMap[sessionId].orEmpty(),
                                    studentName = studentNames[studentId].orEmpty()
                                )
                            }.sortedByDescending { it.timestamp }

                            callback(attendanceRecords, null)
                        }
                        .addOnFailureListener {
                            callback(null, "Failed to load student names: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    callback(null, "Failed to load attendance records: ${it.message}")
                }
        }
        .addOnFailureListener {
            callback(null, "Failed to load sessions: ${it.message}")
        }
}

private fun loadStudentHistory(studentId: String, callback: (List<AttendanceRecord>?, String?) -> Unit) {
    val db = Firebase.firestore

    db.collection("attendance_records")
        .whereEqualTo("studentId", studentId)
        .get()
        .addOnSuccessListener { records ->
            if (records.isEmpty) {
                callback(emptyList(), null)
                return@addOnSuccessListener
            }

            // Get all session IDs
            val sessionIds = records.documents.map { it.getString("sessionId").orEmpty() }.distinct()

            // Get all sessions in one batch
            db.collection("attendance_sessions")
                .whereIn("__name__", sessionIds)
                .get()
                .addOnSuccessListener { sessions ->
                    val sessionMap = sessions.documents.associate {
                        it.id to it.getString("courseName").orEmpty()
                    }

                    val attendanceRecords = records.documents.map { doc ->
                        val sessionId = doc.getString("sessionId").orEmpty()
                        AttendanceRecord(
                            id = doc.id,
                            sessionId = sessionId,
                            studentId = studentId,
                            timestamp = doc.getLong("timestamp") ?: 0,
                            courseName = sessionMap[sessionId].orEmpty()
                        )
                    }.sortedByDescending { it.timestamp }

                    callback(attendanceRecords, null)
                }
                .addOnFailureListener {
                    callback(null, "Failed to load session details: ${it.message}")
                }
        }
        .addOnFailureListener {
            callback(null, "Failed to load attendance records: ${it.message}")
        }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 