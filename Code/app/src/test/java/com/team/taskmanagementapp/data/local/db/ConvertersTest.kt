package com.team.taskmanagementapp.data.local.db

import com.team.taskmanagementapp.data.model.enums.RecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    @Test
    fun recurrenceType_convertsToStringAndBack() {
        val allTypes = listOf(
            RecurrenceType.NONE,
            RecurrenceType.DAILY,
            RecurrenceType.WEEKLY,
            RecurrenceType.MONTHLY,
            RecurrenceType.YEARLY
        )

        for (type in allTypes) {
            val stringVal = converters.fromRecurrenceType(type)
            assertEquals(type.name, stringVal)
            val convertedBack = converters.toRecurrenceType(stringVal)
            assertEquals(type, convertedBack)
        }
    }
}
