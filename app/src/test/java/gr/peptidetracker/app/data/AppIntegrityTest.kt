// Copyright © 2026 Kagon Digital Media & Commerce.
// All rights reserved. See LICENSE for permitted use.

package gr.peptidetracker.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIntegrityTest {
    private val digest = "a".repeat(64)

    @Test
    fun acceptsMatchingSignerCaseInsensitively() {
        assertTrue(
            AppIntegrity.matchesExpectedSigner(
                expected = digest.uppercase(),
                actualSigners = setOf(digest)
            )
        )
    }

    @Test
    fun rejectsMissingOrDifferentSigner() {
        assertFalse(AppIntegrity.matchesExpectedSigner("", setOf(digest)))
        assertFalse(
            AppIntegrity.matchesExpectedSigner(
                expected = digest,
                actualSigners = setOf("b".repeat(64))
            )
        )
    }
}
