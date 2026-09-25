package com.valueappsolutions.prayertimes

import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmHelperTest {
    @Test
    fun testParse() {
        val timeStr = "05:30"
        val parsed = LocalTime.parse(timeStr.substringBefore(" "))
        println(parsed)
    }
}
