package com.example.studentattendanceapp.ui.screens.profile

import androidx.compose.foundation.layout.*
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

data class UserProfile(
    val email: String = "",
    val name: String = "",
    val role: String = "",
    val department: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Load user profile
    LaunchedEffect(Unit) {
        loadUserProfile { profile, errorMessage ->
            isLoading = false
            if (profile != null) {
                userProfile = profile
                name = profile.name
                department = profile.department
            } else {
                error = errorMessage
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings") },
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
            } else {
                userProfile?.let { profile ->
                    if (!isEditing) {
                        ProfileField("Email", profile.email, enabled = false)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileField("Role", profile.role, enabled = false)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileField("Name", profile.name, enabled = false)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileField("Department", profile.department, enabled = false)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Edit Profile")
                        }
                    } else {
                        ProfileField("Email", profile.email, enabled = false)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileField("Role", profile.role, enabled = false)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    name = profile.name
                                    department = profile.department
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    isSaving = true
                                    updateProfile(
                                        name = name,
                                        department = department
                                    ) { success ->
                                        isSaving = false
                                        if (success) {
                                            isEditing = false
                                            loadUserProfile { updatedProfile, _ ->
                                                updatedProfile?.let {
                                                    userProfile = it
                                                }
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, enabled: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun loadUserProfile(callback: (UserProfile?, String?) -> Unit) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(null, "Not authenticated")
        return
    }

    Firebase.firestore.collection("users")
        .document(currentUser.uid)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val profile = UserProfile(
                    email = currentUser.email ?: "",
                    name = document.getString("name") ?: "",
                    role = document.getString("role") ?: "",
                    department = document.getString("department") ?: ""
                )
                callback(profile, null)
            } else {
                callback(null, "Profile not found")
            }
        }
        .addOnFailureListener {
            callback(null, "Failed to load profile: ${it.message}")
        }
}

private fun updateProfile(
    name: String,
    department: String,
    callback: (Boolean) -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    if (currentUser == null) {
        callback(false)
        return
    }

    val updates = hashMapOf<String, Any>(
        "name" to name,
        "department" to department
    )

    Firebase.firestore.collection("users")
        .document(currentUser.uid)
        .update(updates)
        .addOnSuccessListener {
            callback(true)
        }
        .addOnFailureListener {
            callback(false)
        }
} 