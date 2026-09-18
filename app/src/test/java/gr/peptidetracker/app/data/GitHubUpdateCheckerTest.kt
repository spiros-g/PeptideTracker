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
    fun extractsReleaseTagFromGitHubRedirectUrl() {
        assertEquals(
            "v4.8.11",
            GitHubUpdateChecker.extractReleaseTag(
                "https://github.com/spiros-g/PeptideTracker/releases/tag/v4.8.11"
            )
        )
        assertNull(
            GitHubUpdateChecker.extractReleaseTag(
                "https://github.com/spiros-g/PeptideTracker/releases/latest"
            )
        )
    }

    @Test
    fun buildsDeterministicFallbackApkUrl() {
        assertEquals(
            "https://github.com/spiros-g/PeptideTracker/releases/download/v4.8.11/PeptideTracker-v4.8.11.apk",
            GitHubUpdateChecker.buildFallbackApkUrl("v4.8.11")
        )
    }

}
