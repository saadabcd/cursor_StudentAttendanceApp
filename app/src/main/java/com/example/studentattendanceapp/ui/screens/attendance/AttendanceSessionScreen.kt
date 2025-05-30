package com.example.studentattendanceapp.ui.screens.attendance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceSessionScreen() {
    var courseName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var activeSessions by remember { mutableStateOf<List<AttendanceSession>>(emptyList()) }

    // Load active sessions
    LaunchedEffect(Unit) {
        loadActiveSessions { sessions ->
            activeSessions = sessions
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Attendance Session",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = courseName,
            onValueChange = { courseName = it },
            label = { Text("Course Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (courseName.isBlank()) {
                    error = "Please enter a course name"
                    return@Button
                }
                isLoading = true
                error = null
                createAttendanceSession(courseName) { success, message ->
                    isLoading = false
                    if (success) {
                        courseName = ""
                        loadActiveSessions { sessions ->
                            activeSessions = sessions
                        }
                    } else {
                        error = message
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Start New Session")
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Active Sessions",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeSessions) { session ->
                AttendanceSessionCard(
                    session = session,
                    onEndSession = {
                        endAttendanceSession(session.id) { success ->
                            if (success) {
                                loadActiveSessions { updatedSessions ->
                                    activeSessions = updatedSessions
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AttendanceSessionCard(
    session: AttendanceSession,
    onEndSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = session.courseName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Code: ${session.code}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Started: ${formatDate(session.startTime)}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onEndSession,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("End Session")
            }
        }
    }
}

data class AttendanceSession(
    val id: String = "",
    val code: String = "",
    val courseName: String = "",
    val professorId: String = "",
    val startTime: Long = 0,
    val active: Boolean = true
)

private fun createAttendanceSession(courseName: String, callback: (Boolean, String?) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(false, "Not authenticated")
        return
    }

    val sessionCode = generateSessionCode()
    val session = AttendanceSession(
        code = sessionCode,
        courseName = courseName,
        professorId = currentUser.uid,
        startTime = System.currentTimeMillis(),
        active = true
    )

    Firebase.firestore.collection("attendance_sessions")
        .add(session)
        .addOnSuccessListener {
            callback(true, null)
        }
        .addOnFailureListener {
            callback(false, it.message)
        }
}

private fun loadActiveSessions(callback: (List<AttendanceSession>) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(emptyList())
        return
    }

    Firebase.firestore.collection("attendance_sessions")
        .whereEqualTo("professorId", currentUser.uid)
        .whereEqualTo("active", true)
        .get()
        .addOnSuccessListener { documents ->
            val sessions = documents.mapNotNull { doc ->
                doc.toObject(AttendanceSession::class.java).copy(id = doc.id)
            }
            callback(sessions)
        }
        .addOnFailureListener {
            callback(emptyList())
        }
}

private fun endAttendanceSession(sessionId: String, callback: (Boolean) -> Unit) {
    Firebase.firestore.collection("attendance_sessions")
        .document(sessionId)
        .update("active", false)
        .addOnSuccessListener {
            callback(true)
        }
        .addOnFailureListener {
            callback(false)
        }
}

private fun generateSessionCode(): String {
    return (100000..999999).random().toString()
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
} 