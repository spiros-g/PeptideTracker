package gr.peptidetracker.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupCryptoTest {
    @Test
    fun encryptedBackupRoundTrips() {
        val raw = """{"schema":9,"entries":[{"id":1}]}"""
        val bytes = BackupCrypto.encrypt(raw, "correct horse".toCharArray())
        assertTrue(BackupCrypto.isEncrypted(bytes))
        assertEquals(raw, BackupCrypto.decrypt(bytes, "correct horse".toCharArray()))
    }

    @Test
    fun wrongPasswordDoesNotDecrypt() {
        val bytes = BackupCrypto.encrypt("secret", "abcdef".toCharArray())
        assertNull(BackupCrypto.decrypt(bytes, "wrongxx".toCharArray()))
    }

    @Test
    fun plainJsonIsNotMarkedEncrypted() {
        assertFalse(BackupCrypto.isEncrypted("{}".toByteArray()))
    }
}
