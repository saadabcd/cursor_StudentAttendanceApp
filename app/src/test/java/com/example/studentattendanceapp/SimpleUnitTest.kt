package com.example.studentattendanceapp

import org.junit.Test
import org.junit.Assert.*

class SimpleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun string_length_isCorrect() {
        val testString = "StudentAttendance"
        assertEquals(16, testString.length)
    }
} 