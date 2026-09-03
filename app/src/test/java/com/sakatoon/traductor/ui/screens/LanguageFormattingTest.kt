package com.sakatoon.traductor.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageFormattingTest {
    @Test
    fun `language code is displayed as a capitalized Spanish name`() {
        assertEquals("Inglés", languageName("en"))
        assertEquals("Español", languageName("es"))
    }
}
