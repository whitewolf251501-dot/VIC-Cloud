package com.velmora.vic

import com.velmora.vic.skills.PhoneSkillManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneSkillsTest {

    @Test
    fun testParseWhatsAppDraft_validStandardPrompt() {
        val prompt = "Send WhatsApp to Alex saying I will arrive in 10 minutes"
        val draft = PhoneSkillManager.parseWhatsAppDraft(prompt)
        assertNotNull("Draft should not be null", draft)
        assertEquals("Alex", draft?.recipient)
        assertEquals("I will arrive in 10 minutes", draft?.message)
    }

    @Test
    fun testParseWhatsAppDraft_validWithPhoneNumber() {
        val prompt = "Send WhatsApp to +919876543210 that The system is ready"
        val draft = PhoneSkillManager.parseWhatsAppDraft(prompt)
        assertNotNull("Draft should not be null", draft)
        assertEquals("+919876543210", draft?.recipient)
        assertEquals("The system is ready", draft?.message)
    }

    @Test
    fun testParseWhatsAppDraft_invalidOrIncompleteQuery() {
        val prompt = "Open WhatsApp"
        val draft = PhoneSkillManager.parseWhatsAppDraft(prompt)
        assertNull("Incomplete query should return null", draft)
    }

    @Test
    fun testParseWhatsAppDraft_withCustomMessageKeyword() {
        val prompt = "WhatsApp Mom with message Meeting rescheduled to 4 PM"
        val draft = PhoneSkillManager.parseWhatsAppDraft(prompt)
        assertNotNull("Draft should not be null", draft)
        assertEquals("Mom", draft?.recipient)
        assertEquals("Meeting rescheduled to 4 PM", draft?.message)
    }
}
