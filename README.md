# Student Attendance App - Technical Documentation

## Overview
A smart attendance management system built with Kotlin, Jetpack Compose, and Firebase. The app provides role-based functionality for professors and students to manage and track class attendance in real-time.

## Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Backend**: Firebase (Authentication, Realtime Database, Firestore)
- **Architecture**: MVVM with Clean Architecture
- **State Management**: ViewModel with StateFlow

## Color System
```kotlin
val PrimaryBlue = Color(0xFF1976D2)
val SecondaryTeal = Color(0xFF00897B)
val AccentAmber = Color(0xFFFFC107)
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = Color(0xFFF44336)
```

## Screen Navigation Structure

### 1. Welcome Screen (`WelcomeScreen.kt`)
Entry point of the app showing app logo and brand.
- **Navigation**:
  - → Sign In Screen (returning users)
  - → Sign Up Screen (new users)

### 2. Authentication Screens
#### 2.1 Sign Up Screen (`SignUpScreen.kt`)
New user registration form.
- **Fields**:
  - Email (with validation)
  - Password (with strength requirements)
  - Full Name
  - Role Selection (Professor/Student)
  - Department/Filière (for students)
- **Navigation**:
  - → Role-specific Dashboard
  - ← Back to Welcome Screen

#### 2.2 Sign In Screen (`SignInScreen.kt`)
Returning user authentication.
- **Fields**:
  - Email
  - Password
  - Remember Me option
- **Navigation**:
  - → Role-specific Dashboard
  - ← Back to Welcome Screen
  - → Password Reset Screen

### 3. Professor Screens
#### 3.1 Professor Dashboard (`ProfessorDashboard.kt`)
Main screen for professors after authentication.
- **Features**:
  - Weekly timetable view
  - Active sessions counter
  - Quick actions menu
  - Recent attendance sessions
- **Navigation**:
  - → Timetable Editor
  - → Attendance Session
  - → History View
  - → Profile Settings

#### 3.2 Timetable Editor (`TimetableEditor.kt`)
Interface for managing class schedules.
- **Features**:
  - Week view calendar
  - Add/Edit/Delete class slots
  - Class details form
  - Recurring schedule options
- **Navigation**:
  - ← Back to Dashboard
  - → Class Details

#### 3.3 Attendance Session (`AttendanceSession.kt`)
Active attendance tracking interface.
- **Features**:
  - Real-time student check-ins
  - Session timer
  - Present/Absent counters
  - Student list with status
- **Navigation**:
  - ← Back to Dashboard
  - → Session Summary

#### 3.4 Attendance History (`ProfessorHistory.kt`)
Historical attendance records view.
- **Features**:
  - Filter by date/class
  - Export functionality
  - Detailed statistics
  - Student performance metrics
- **Navigation**:
  - ← Back to Dashboard
  - → Session Details

### 4. Student Screens
#### 4.1 Student Dashboard (`StudentDashboard.kt`)
Main screen for students after authentication.
- **Features**:
  - Department timetable
  - Active session alerts
  - Personal attendance stats
  - Quick check-in button
- **Navigation**:
  - → Attendance Check-in
  - → Personal History
  - → Profile Settings

#### 4.2 Attendance Check-in (`StudentCheckin.kt`)
Interface for marking attendance.
- **Features**:
  - Active session details
  - Check-in button
  - Confirmation status
  - Session timer
- **Navigation**:
  - ← Back to Dashboard
  - → Success Confirmation

#### 4.3 Student History (`StudentHistory.kt`)
Personal attendance record view.
- **Features**:
  - Attendance percentage
  - Course-wise breakdown
  - Calendar view
  - Absence reports
- **Navigation**:
  - ← Back to Dashboard
  - → Course Details

## Data Models

### User
```kotlin
data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val department: String? = null
)

enum class UserRole {
    PROFESSOR,
    STUDENT
}
```

### Course
```kotlin
data class Course(
    val id: String,
    val name: String,
    val professorId: String,
    val department: String,
    val schedule: List<TimeSlot>
)
```

### AttendanceSession
```kotlin
data class AttendanceSession(
    val id: String,
    val courseId: String,
    val date: LocalDateTime,
    val duration: Duration,
    val status: SessionStatus,
    val attendees: List<Attendance>
)
```

### Attendance
```kotlin
data class Attendance(
    val sessionId: String,
    val studentId: String,
    val timestamp: LocalDateTime,
    val status: AttendanceStatus
)
```

## Firebase Structure

### Realtime Database
```
/users
  /{userId}
    - basic info
    - role
    - department

/courses
  /{courseId}
    - details
    - schedule
    - enrolled students

/sessions
  /{sessionId}
    - status
    - start time
    - attendees
```

### Firestore Collections
- users
- courses
- attendance_sessions
- attendance_records

## Implementation Guidelines

1. **State Management**
   - Use `ViewModel` for screen-level state
   - Implement `StateFlow` for reactive updates
   - Handle configuration changes properly

2. **Firebase Integration**
   - Initialize Firebase in Application class
   - Use coroutines for async operations
   - Implement offline persistence

3. **UI Components**
   - Follow Material Design 3 guidelines
   - Use custom theme colors
   - Implement dark mode support
   - Handle different screen sizes

4. **Security Rules**
   - Implement proper Firebase security rules
   - Validate user roles for actions
   - Secure attendance check-in process

5. **Error Handling**
   - Implement proper error states
   - Show user-friendly error messages
   - Handle network connectivity issues

## Getting Started

1. Clone the repository
2. Add Firebase configuration file
3. Set up Firebase project with necessary services
4. Run the app in Android Studio

## Dependencies
```kotlin
// Add to app/build.gradle.kts
dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.material3:material3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
``` 