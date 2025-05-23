# Steps to Set Up and Run the Student Attendance App

## 1. Project Setup
1. Open Android Studio
2. Create a new project with:
   - Name: "Student Attendance App"
   - Package name: `com.example.studentattendanceapp`
   - Language: Kotlin
   - Minimum SDK: API 24 (Android 7.0)
   - Use Jetpack Compose: Yes

## 2. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project:
   - Name: "Student Attendance App"
   - Disable Google Analytics (optional)
3. Add Android app:
   - Package name: `com.example.studentattendanceapp`
   - App nickname: "Student Attendance App"
   - Debug signing certificate SHA-1: (optional for now)
4. Download `google-services.json`
5. Place `google-services.json` in the `app` directory of your project
6. Enable Authentication:
   - Go to Authentication section
   - Click "Get Started"
   - Enable Email/Password sign-in method
7. Set up Firestore Database:
   - Go to Firestore Database section
   - Click "Create Database"
   - Choose "Start in test mode"
   - Select region closest to you
8. Set up Realtime Database:
   - Go to Realtime Database section
   - Click "Create Database"
   - Choose "Start in test mode"
   - Select region closest to you

## 3. Project Files Structure
Ensure you have the following files in place:

```
app/
├── build.gradle.kts
├── src/main/
│   ├── AndroidManifest.xml
│   │   ├── java/com/example/studentattendanceapp/
│   │   │   ├── MainActivity.kt
│   │   │   ├── StudentAttendanceApp.kt
│   │   │   ├── navigation/
│   │   │   │   └── NavGraph.kt
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   └── welcome/
│   │   │   │   │       └── WelcomeScreen.kt
│   │   │   │   └── theme/
│   │   │   │   └── Color.kt
│   │   │   └── theme/
│   │   │       └── Theme.kt
```

## 4. Dependencies Check
Ensure your `build.gradle.kts` files are properly set up:

1. Root `build.gradle.kts` should have:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

2. App `build.gradle.kts` should have:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    
    // Other dependencies as shown in the file
}
```

## 5. Manifest Check
Ensure `AndroidManifest.xml` has:
- Application name set to `.StudentAttendanceApp`
- Internet permission
- Main activity properly declared

## 6. Running the App
1. Connect an Android device or start an emulator
2. Click "Sync Project with Gradle Files"
3. Wait for the sync to complete
4. Click "Run" (green play button) or press Shift + F10

## 7. Expected Result
When you run the app, you should see:
1. Welcome screen with:
   - Title: "Welcome to the Student Attendance App"
   - Subtitle: "Track attendance seamlessly"
   - Two buttons:
     - "Sign In" button
     - "Create Account" button
2. Clicking the buttons should (currently) navigate to empty screens

## 8. Troubleshooting
If you encounter issues:
1. Check if Firebase is properly initialized:
   - Verify `google-services.json` is in the correct location
   - Check Logcat for Firebase initialization messages
2. Check build errors:
   - Look for any red underlines in code
   - Check the "Build" tab for specific error messages
3. Verify dependencies:
   - Run "Clean Project" (Build > Clean Project)
   - Run "Rebuild Project" (Build > Rebuild Project)

## 9. Firebase Authentication Setup
1. Create test accounts in Firebase Console:
   - Go to Firebase Console > Authentication
   - Click "Add User"
   - Create a professor test account:
     ```
     Email: professor@test.com
     Password: professor123
     ```
   - Create a student test account:
     ```
     Email: student@test.com
     Password: student123
     ```

2. Set up user roles in Firestore:
   - Go to Firestore Database
   - Create a new collection called "users"
   - Add a document for the professor:
     ```
     Document ID: [copy the UID from Authentication]
     Fields:
       - email: "professor@test.com"
       - name: "Test Professor"
       - role: "PROFESSOR"
     ```
   - Add a document for the student:
     ```
     Document ID: [copy the UID from Authentication]
     Fields:
       - email: "student@test.com"
       - name: "Test Student"
       - role: "STUDENT"
       - department: "Computer Science"
     ```

## 10. Testing the App
1. Run the app
2. Click "Sign In" on the welcome screen
3. Test professor login:
   - Email: professor@test.com
   - Password: professor123
   - Should navigate to Professor Dashboard
4. Test student login:
   - Email: student@test.com
   - Password: student123
   - Should navigate to Student Dashboard

## 11. Current Features
- Welcome screen with navigation
- Sign In screen with:
  - Email and password fields
  - Loading state
  - Error handling
  - Navigation to Sign Up
  - Firebase authentication

## Next Steps
1. Implement Sign Up screen
2. Create Professor Dashboard
3. Create Student Dashboard
4. Implement role-based navigation

## Troubleshooting Common Issues
1. Firebase Authentication Issues:
   - Check if your device/emulator has internet connection
   - Verify Firebase Authentication is enabled in console
   - Check Logcat for specific authentication errors
   - Verify test accounts are created correctly

2. Navigation Issues:
   - Check if NavController is properly passed to screens
   - Verify route names match exactly
   - Look for navigation-related errors in Logcat

3. Gradle Sync Issues:
   - Check if all dependencies are properly added
   - Verify Google Services plugin is applied
   - Make sure `google-services.json` is in the correct location

## 12. App Flow Diagram and Explanation

```
                                    ┌─────────────────┐
                                    │  Welcome Screen │
                                    └────────┬────────┘
                                             │
                              ┌──────────────┴──────────────┐
                              │                             │
                        ┌─────▼─────┐               ┌───────▼───────┐
                        │  Sign In  │               │    Sign Up    │
                        └─────┬─────┘               └───────┬───────┘
                              │                             │
                  ┌──────────┴──────────┐         ┌────────┴────────┐
                  │   Authentication    │         │  Create Account  │
                  └──────────┬──────────┘         └────────┬────────┘
                             │                             │
               ┌─────────────┴─────────────┐              │
               │                           │              │
     ┌─────────▼────────┐        ┌────────▼─────────┐   │
     │Professor Dashboard│        │Student Dashboard  │   │
     └─────────┬────────┘        └────────┬─────────┘   │
               │                           │              │
    ┌──────────┼──────────┐    ┌─────────┼─────────┐    │
    │          │          │    │         │         │    │
┌───▼───┐ ┌────▼────┐ ┌──▼──┐ ┌───▼───┐ ┌───▼────┐    │
│Classes│ │Sessions │ │Stats│ │Check-in│ │History │    │
└───────┘ └─────────┘ └─────┘ └───────┘ └────────┘    │
    │          │          │        │         │         │
    └──────────┴──────────┴────────┴─────────┴─────────┘
```

### Flow Description:

1. **Entry Point: Welcome Screen**
   - First screen users see when opening the app
   - Options: "Sign In" or "Create Account"

2. **Authentication Flow**
   - **Sign In Path**:
     - Enter email and password
     - Firebase authenticates credentials
     - Retrieves user role from Firestore
     - Navigates to role-specific dashboard
   
   - **Sign Up Path**:
     - Enter email, password, name
     - Select role (Professor/Student)
     - If Student, select department
     - Creates Firebase Auth account
     - Stores user data in Firestore
     - Navigates to role-specific dashboard

3. **Professor Flow**
   - Dashboard shows:
     - Weekly timetable
     - Active sessions counter
     - Quick actions menu
   
   - Can access:
     - Classes: Manage course schedule
     - Sessions: Start/monitor attendance
     - Stats: View attendance statistics

4. **Student Flow**
   - Dashboard shows:
     - Department timetable
     - Active sessions
     - Personal attendance stats
   
   - Can access:
     - Check-in: Mark attendance
     - History: View attendance record

### Navigation Rules:
1. After successful authentication:
   - Welcome screen is removed from back stack
   - Can't go back to Sign In/Sign Up screens
   - Back button exits app from dashboard

2. Within role-specific sections:
   - Can navigate freely between features
   - Back button returns to dashboard
   - Menu/bottom navigation for quick access

### Data Flow:
1. **Authentication Data**:
   ```
   Firebase Auth ─► User ID ─► Firestore User Profile
   ```

2. **Professor Data**:
   ```
   Courses ─► Sessions ─► Attendance Records
      ▲            │
      └────────────┘
   ```

3. **Student Data**:
   ```
   Department ─► Courses ─► Active Sessions
                    │
                    ▼
              Attendance Records
   ```

Would you like me to explain any specific part of the flow in more detail?

===============
Required Actions From You:
1. Create the test accounts in Firebase Console exactly as specified in section 9
2. Create the Firestore documents for user roles as specified
3. Test the login functionality with both test accounts
4. Note any error messages or issues you encounter during testing

Let me know once you've completed these steps, and we can proceed with implementing the Sign Up screen or address any issues you encounter. 