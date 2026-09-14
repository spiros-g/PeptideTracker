package gr.peptidetracker.app.data

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private val magic = "PTGR1".toByteArray(StandardCharsets.US_ASCII)
    private const val SALT_SIZE = 16
    private const val IV_SIZE = 12
    private const val KEY_BITS = 256
    private const val ITERATIONS = 150_000
    private const val GCM_TAG_BITS = 128

    fun isEncrypted(bytes: ByteArray): Boolean =
        bytes.size > magic.size + SALT_SIZE + IV_SIZE &&
            bytes.copyOfRange(0, magic.size).contentEquals(magic)

    fun encrypt(raw: String, password: CharArray): ByteArray {
        require(password.size >= 6)
        val random = SecureRandom()
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val iv = ByteArray(IV_SIZE).also(random::nextBytes)
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        cipher.updateAAD(magic)
        val encrypted = cipher.doFinal(raw.toByteArray(StandardCharsets.UTF_8))

        return ByteBuffer
            .allocate(magic.size + salt.size + iv.size + encrypted.size)
            .put(magic)
            .put(salt)
            .put(iv)
            .put(encrypted)
            .array()
    }

    fun decrypt(bytes: ByteArray, password: CharArray): String? {
        if (!isEncrypted(bytes) || password.isEmpty()) return null
        return try {
            val buffer = ByteBuffer.wrap(bytes)
            val actualMagic = ByteArray(magic.size).also(buffer::get)
            if (!actualMagic.contentEquals(magic)) return null
            val salt = ByteArray(SALT_SIZE).also(buffer::get)
            val iv = ByteArray(IV_SIZE).also(buffer::get)
            val encrypted = ByteArray(buffer.remaining()).also(buffer::get)
            val key = deriveKey(password, salt)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
            cipher.updateAAD(magic)
            String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
        } catch (_: AEADBadTagException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_BITS)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
            SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
