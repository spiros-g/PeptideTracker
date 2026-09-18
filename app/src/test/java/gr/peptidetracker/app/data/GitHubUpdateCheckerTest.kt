package gr.peptidetracker.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdateCheckerTest {
    @Test
    fun detectsNewerPatchVersion() {
        assertTrue(GitHubUpdateChecker.isNewerVersion("v4.7.1", "4.7.0"))
    }

    @Test
    fun detectsNewerMinorVersion() {
        assertTrue(GitHubUpdateChecker.isNewerVersion("4.8.0", "4.7.9"))
    }

    @Test
    fun ignoresSameVersion() {
        assertFalse(GitHubUpdateChecker.isNewerVersion("v4.7.0", "4.7.0"))
    }

    @Test
    fun ignoresOlderVersion() {
        assertFalse(GitHubUpdateChecker.isNewerVersion("4.6.9", "4.7.0"))
    }

    @Test
    fun comparesDifferentVersionLengths() {
        assertTrue(GitHubUpdateChecker.isNewerVersion("4.7.1", "4.7"))
        assertFalse(GitHubUpdateChecker.isNewerVersion("4.7", "4.7.1"))
    }

    @Test
    fun normalizesGitHubSha256Digest() {
        val digest = "A".repeat(64)
        assertEquals(
            "a".repeat(64),
            GitHubUpdateChecker.normalizeSha256("sha256:" + digest)
        )
    }

    @Test
    fun rejectsMalformedDigest() {
        assertNull(GitHubUpdateChecker.normalizeSha256("sha256:not-a-digest"))
    }
}
