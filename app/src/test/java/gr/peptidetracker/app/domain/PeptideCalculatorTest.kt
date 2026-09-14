package gr.peptidetracker.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PeptideCalculatorTest {
    @Test fun standardConcentration() {
        val r = PeptideCalculator.concentration(10.0, false, 2.0, 100)
        assertEquals(5.0, r.mgPerMl, 0.000001)
        assertEquals(5000.0, r.mcgPerMl, 0.000001)
        assertEquals(50.0, r.mcgPerUnit, 0.000001)
    }
    @Test fun standardDose() {
        val r = PeptideCalculator.dose(10.0, 2.0, 250.0, false, 100)
        assertEquals(0.05, r.volumeMl, 0.000001)
        assertEquals(5.0, r.syringeUnits, 0.000001)
    }
    @Test fun reverseDose() {
        val r = PeptideCalculator.reverse(10.0, 2.0, 5.0, 100)
        assertEquals(0.05, r.volumeMl, 0.000001)
        assertEquals(250.0, r.amountMcg, 0.000001)
        assertEquals(0.25, r.amountMg, 0.000001)
    }
    @Test fun u40AndMcgInput() {
        assertEquals(125.0, PeptideCalculator.concentration(5000.0, true, 1.0, 40).mcgPerUnit, 0.000001)
    }
    @Test fun rejectsInvalidValues() {
        assertThrows(IllegalArgumentException::class.java) { PeptideCalculator.dose(10.0, 0.0, 1.0, true, 100) }
    }
}
