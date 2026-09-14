package gr.peptidetracker.app.domain

import kotlin.math.round

data class ConcentrationResult(val mgPerMl: Double, val mcgPerMl: Double, val mcgPerUnit: Double)
data class DoseResult(val concentrationMgPerMl: Double, val volumeMl: Double, val syringeUnits: Double)
data class ReverseResult(val volumeMl: Double, val amountMcg: Double, val amountMg: Double)

object PeptideCalculator {
    fun concentration(vialAmount: Double, vialIsMcg: Boolean, diluentMl: Double, syringeUnitsPerMl: Int): ConcentrationResult {
        require(vialAmount > 0 && diluentMl > 0 && syringeUnitsPerMl > 0)
        val mg = if (vialIsMcg) vialAmount / 1000.0 else vialAmount
        val mgMl = mg / diluentMl
        return ConcentrationResult(mgMl, mgMl * 1000.0, mgMl * 1000.0 / syringeUnitsPerMl)
    }

    fun dose(vialMg: Double, diluentMl: Double, target: Double, targetIsMg: Boolean, syringeUnitsPerMl: Int): DoseResult {
        require(vialMg > 0 && diluentMl > 0 && target > 0 && syringeUnitsPerMl > 0)
        val concentration = vialMg / diluentMl
        val targetMg = if (targetIsMg) target else target / 1000.0
        val volume = targetMg / concentration
        return DoseResult(concentration, volume, volume * syringeUnitsPerMl)
    }

    fun reverse(vialMg: Double, diluentMl: Double, units: Double, syringeUnitsPerMl: Int): ReverseResult {
        require(vialMg > 0 && diluentMl > 0 && units >= 0 && syringeUnitsPerMl > 0)
        val volume = units / syringeUnitsPerMl
        val mg = (vialMg / diluentMl) * volume
        return ReverseResult(volume, mg * 1000.0, mg)
    }

    fun format(value: Double, decimals: Int = 4): String {
        val factor = Math.pow(10.0, decimals.toDouble())
        return (round(value * factor) / factor).toString().trimEnd('0').trimEnd('.')
    }
}
