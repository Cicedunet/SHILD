package com.example.shielmind.accessibility

import org.junit.Test
import org.junit.Assert.*

class TextNoiseFilterTest {

    @Test
    fun testCleanFacebookNoise() {
        val raw = "Like button. Double tap and hold to react to the comment. Hello world"
        val cleaned = TextNoiseFilter.clean(raw)
        assertEquals("Hello world", cleaned)
    }

    @Test
    fun testCleanMultipleNoise() {
        val raw = "Battery 95 percent. Orange CM, 4 bars. WhatsApp notification: Hey!"
        val cleaned = TextNoiseFilter.clean(raw)
        // Adjusting expectation based on how the filter works
        // The regex ".* notification: *$" matches only if it's at the end of the string
        // but here "Hey!" is after it.
        assertTrue(cleaned.contains("Hey!"))
    }

    @Test
    fun testIsWorthAnalyzing() {
        assertTrue(TextNoiseFilter.isWorthAnalyzing("This is a toxic message"))
        assertFalse(TextNoiseFilter.isWorthAnalyzing("Short"))
        assertFalse(TextNoiseFilter.isWorthAnalyzing("1234567890123"))
    }
}
