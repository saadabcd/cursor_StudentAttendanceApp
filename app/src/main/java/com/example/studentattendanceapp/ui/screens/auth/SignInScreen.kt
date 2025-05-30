package com.example.studentattendanceapp.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studentattendanceapp.navigation.Screen
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private const val TAG = "SignInScreen"

@Composable
fun SignInScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
        Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isLoading = true
                error = null
                Log.d(TAG, "Attempting to sign in with email: $email")
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val userId = authResult.user?.uid
                        Log.d(TAG, "Sign in successful. User ID: $userId")
                        if (userId != null) {
                            Log.d(TAG, "Fetching user role from Firestore...")
                            Firebase.firestore.collection("users").document(userId)
                                .get()
                                .addOnSuccessListener { document ->
                        isLoading = false
                                    if (!document.exists()) {
                                        Log.e(TAG, "User document does not exist in Firestore")
                                        error = "User profile not found"
                                        Firebase.auth.signOut()
                                        return@addOnSuccessListener
                                    }
                                    val role = document.getString("role")
                                    Log.d(TAG, "Retrieved user role: $role")
                                    when (role?.uppercase()) {
                                        "PROFESSOR" -> {
                                            Log.d(TAG, "Navigating to Professor Dashboard")
                            navController.navigate(Screen.ProfessorDashboard.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                                        }
                                        "STUDENT" -> {
                                            Log.d(TAG, "Navigating to Student Dashboard")
                                            navController.navigate(Screen.StudentDashboard.route) {
                                                popUpTo(Screen.Welcome.route) { inclusive = true }
                                            }
                                        }
                                        else -> {
                                            Log.e(TAG, "Invalid role found: $role")
                                            error = "Invalid user role: $role"
                                            Firebase.auth.signOut()
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e(TAG, "Failed to fetch user role", it)
                                    isLoading = false
                                    error = "Failed to fetch user role: ${it.message}"
                                    Firebase.auth.signOut()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Sign in failed", it)
                        isLoading = false
                        error = "Sign in failed: ${it.message}"
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate(Screen.SignUp.route) }
        ) {
            Text("Don't have an account? Sign Up")
        }
    }
} 