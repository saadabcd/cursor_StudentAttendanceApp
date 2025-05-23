package com.example.studentattendanceapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.studentattendanceapp.ui.screens.welcome.WelcomeScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object SignIn : Screen("signIn")
    object SignUp : Screen("signUp")
    object ProfessorDashboard : Screen("professorDashboard")
    object StudentDashboard : Screen("studentDashboard")
    object TimetableEditor : Screen("timetableEditor")
    object AttendanceSession : Screen("attendanceSession")
    object AttendanceHistory : Screen("attendanceHistory")
    object StudentCheckin : Screen("studentCheckin")
    object StudentHistory : Screen("studentHistory")
    object ProfileSettings : Screen("profileSettings")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController)
        }
        
        composable(Screen.SignIn.route) {
            // SignInScreen(navController)
        }
        
        composable(Screen.SignUp.route) {
            // SignUpScreen(navController)
        }
        
        composable(Screen.ProfessorDashboard.route) {
            // ProfessorDashboard(navController)
        }
        
        composable(Screen.StudentDashboard.route) {
            // StudentDashboard(navController)
        }
        
        composable(Screen.TimetableEditor.route) {
            // TimetableEditor(navController)
        }
        
        composable(Screen.AttendanceSession.route) {
            // AttendanceSession(navController)
        }
        
        composable(Screen.AttendanceHistory.route) {
            // AttendanceHistory(navController)
        }
        
        composable(Screen.StudentCheckin.route) {
            // StudentCheckin(navController)
        }
        
        composable(Screen.StudentHistory.route) {
            // StudentHistory(navController)
        }
        
        composable(Screen.ProfileSettings.route) {
            // ProfileSettings(navController)
        }
    }
} 