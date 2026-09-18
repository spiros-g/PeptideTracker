package gr.peptidetracker.app.i18n

import gr.peptidetracker.app.data.peptideCatalog
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppTextTest {
    @After
    fun resetLanguage() {
        setLanguageOverride(null)
    }

    @Test
    fun explicitLanguageOverrideSwitchesUiCopy() {
        setLanguageOverride("el")
        assertEquals("Ρυθμίσεις", t("Ρυθμίσεις"))

        setLanguageOverride("en")
        assertEquals("Settings", t("Ρυθμίσεις"))
    }

    @Test
    fun peptideCatalogRefreshesWhenLanguageChanges() {
        setLanguageOverride("el")
        val greekCategory = peptideCatalog.first().category

        setLanguageOverride("en")
        val englishCategory = peptideCatalog.first().category

        assertNotEquals(greekCategory, englishCategory)
        assertEquals("Metabolism & Incretins", englishCategory)
    }
}
