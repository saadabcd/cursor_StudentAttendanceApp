package com.example.studentattendanceapp

import android.app.Application
import com.google.firebase.FirebaseApp

class StudentAttendanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 