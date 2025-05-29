package com.example.studentattendanceapp.ui.screens.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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

data class ClassSchedule(
    val id: String = "",
    val courseName: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val room: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableEditorScreen(navController: NavController) {
    var schedules by remember { mutableStateOf<List<ClassSchedule>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Load timetable
    LaunchedEffect(Unit) {
        loadTimetable { result, errorMessage ->
            isLoading = false
            if (result != null) {
                schedules = result
            } else {
                error = errorMessage
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable Editor") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Class")
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
            } else if (schedules.isEmpty()) {
                Text(
                    text = "No classes scheduled yet",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(schedules.sortedBy { "${it.dayOfWeek}${it.startTime}" }) { schedule ->
                        ClassScheduleCard(
                            schedule = schedule,
                            onDelete = { scheduleId ->
                                deleteSchedule(scheduleId) { success ->
                                    if (success) {
                                        schedules = schedules.filter { it.id != scheduleId }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddClassDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { newSchedule ->
                    addSchedule(newSchedule) { success, id ->
                        if (success && id != null) {
                            schedules = schedules + newSchedule.copy(id = id)
                            showAddDialog = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ClassScheduleCard(
    schedule: ClassSchedule,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = schedule.courseName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${schedule.dayOfWeek}, ${schedule.startTime} - ${schedule.endTime}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Room: ${schedule.room}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = { onDelete(schedule.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassDialog(
    onDismiss: () -> Unit,
    onAdd: (ClassSchedule) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Class") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dayOfWeek,
                    onValueChange = { dayOfWeek = it },
                    label = { Text("Day of Week") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End Time (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (courseName.isNotBlank() && dayOfWeek.isNotBlank() &&
                        startTime.isNotBlank() && endTime.isNotBlank() && room.isNotBlank()
                    ) {
                        onAdd(
                            ClassSchedule(
                                courseName = courseName,
                                dayOfWeek = dayOfWeek,
                                startTime = startTime,
                                endTime = endTime,
                                room = room
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun loadTimetable(callback: (List<ClassSchedule>?, String?) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(null, "Not authenticated")
        return
    }

    Firebase.firestore.collection("class_schedules")
        .whereEqualTo("professorId", currentUser.uid)
        .get()
        .addOnSuccessListener { documents ->
            val schedules = documents.map { doc ->
                ClassSchedule(
                    id = doc.id,
                    courseName = doc.getString("courseName") ?: "",
                    dayOfWeek = doc.getString("dayOfWeek") ?: "",
                    startTime = doc.getString("startTime") ?: "",
                    endTime = doc.getString("endTime") ?: "",
                    room = doc.getString("room") ?: ""
                )
            }
            callback(schedules, null)
        }
        .addOnFailureListener {
            callback(null, "Failed to load timetable: ${it.message}")
        }
}

private fun addSchedule(schedule: ClassSchedule, callback: (Boolean, String?) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(false, null)
        return
    }

    val scheduleData = hashMapOf(
        "professorId" to currentUser.uid,
        "courseName" to schedule.courseName,
        "dayOfWeek" to schedule.dayOfWeek,
        "startTime" to schedule.startTime,
        "endTime" to schedule.endTime,
        "room" to schedule.room
    )

    Firebase.firestore.collection("class_schedules")
        .add(scheduleData)
        .addOnSuccessListener {
            callback(true, it.id)
        }
        .addOnFailureListener {
            callback(false, null)
        }
}

private fun deleteSchedule(scheduleId: String, callback: (Boolean) -> Unit) {
    Firebase.firestore.collection("class_schedules")
        .document(scheduleId)
        .delete()
        .addOnSuccessListener {
            callback(true)
        }
        .addOnFailureListener {
            callback(false)
        }
} 