package com.valueappsolutions.prayertimes

import org.junit.Test
import java.time.LocalTime

class AlarmHelperTest2 {
    @Test
    fun testParse() {
        val timeStrs = listOf("05:30 (CDT)", "05:30", "15:30")
        timeStrs.forEach {
            println("Parsed '$it' -> ${LocalTime.parse(it.substringBefore(" "))}")
        }
    }
}
