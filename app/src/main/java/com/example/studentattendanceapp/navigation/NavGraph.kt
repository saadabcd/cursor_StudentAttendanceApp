package com.example.studentattendanceapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.studentattendanceapp.ui.screens.welcome.WelcomeScreen
import com.example.studentattendanceapp.ui.screens.auth.SignInScreen
import com.example.studentattendanceapp.ui.screens.dashboard.ProfessorDashboard
import com.example.studentattendanceapp.ui.screens.dashboard.StudentDashboard
import com.example.studentattendanceapp.ui.screens.attendance.AttendanceSessionScreen
import com.example.studentattendanceapp.ui.screens.attendance.StudentCheckinScreen
import com.example.studentattendanceapp.ui.screens.profile.ProfileSettingsScreen
import com.example.studentattendanceapp.ui.screens.attendance.AttendanceHistoryScreen
import com.example.studentattendanceapp.ui.screens.timetable.TimetableEditorScreen

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
            SignInScreen(navController)
        }
        
        composable(Screen.SignUp.route) {
            // SignUpScreen(navController)
        }
        
        composable(Screen.ProfessorDashboard.route) {
            ProfessorDashboard(navController)
        }
        
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(navController)
        }
        
        composable(Screen.TimetableEditor.route) {
            TimetableEditorScreen(navController)
        }
        
        composable(Screen.AttendanceSession.route) {
            AttendanceSessionScreen(navController)
        }
        
        composable(Screen.AttendanceHistory.route) {
            AttendanceHistoryScreen(navController)
        }
        
        composable(Screen.StudentCheckin.route) {
            StudentCheckinScreen(navController)
        }
        
        composable(Screen.StudentHistory.route) {
            AttendanceHistoryScreen(navController)
        }
        
        composable(Screen.ProfileSettings.route) {
            ProfileSettingsScreen(navController)
        }
    }
} 