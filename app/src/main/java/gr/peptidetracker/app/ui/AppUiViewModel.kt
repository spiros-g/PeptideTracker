package gr.peptidetracker.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import gr.peptidetracker.app.ui.screens.CalculatorPreset

class AppUiViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var trackerSection by mutableIntStateOf(savedStateHandle["tracker_section"] ?: 0)
        private set

    var newLogRequest by mutableIntStateOf(savedStateHandle["new_log_request"] ?: 0)
        private set

    var dataToolsRequest by mutableIntStateOf(savedStateHandle["data_tools_request"] ?: 0)
        private set

    var inventoryPreset by mutableStateOf(savedStateHandle.get<String>("inventory_preset"))
        private set

    var calculatorPreset by mutableStateOf(readCalculatorPreset())
        private set

    fun setTrackerSection(value: Int) {
        trackerSection = value.coerceIn(0, 3)
        savedStateHandle["tracker_section"] = trackerSection
    }

    fun requestNewLog() {
        setTrackerSection(0)
        newLogRequest += 1
        savedStateHandle["new_log_request"] = newLogRequest
    }

    fun consumeNewLogRequest() {
        newLogRequest = 0
        savedStateHandle["new_log_request"] = 0
    }

    fun requestDataTools() {
        dataToolsRequest += 1
        savedStateHandle["data_tools_request"] = dataToolsRequest
    }

    fun consumeDataToolsRequest() {
        dataToolsRequest = 0
        savedStateHandle["data_tools_request"] = 0
    }

    fun setInventoryPreset(value: String?) {
        inventoryPreset = value
        savedStateHandle["inventory_preset"] = value
    }

    fun consumeInventoryPreset() = setInventoryPreset(null)

    fun setCalculatorPreset(value: CalculatorPreset?) {
        calculatorPreset = value
        savedStateHandle["calculator_peptide"] = value?.peptideName
        savedStateHandle["calculator_vial_mg"] = value?.vialMg
        savedStateHandle["calculator_diluent_ml"] = value?.diluentMl
        savedStateHandle["calculator_syringe"] = value?.syringeUnitsPerMl
    }

    fun consumeCalculatorPreset() = setCalculatorPreset(null)

    private fun readCalculatorPreset(): CalculatorPreset? {
        val peptide = savedStateHandle.get<String>("calculator_peptide") ?: return null
        return CalculatorPreset(
            peptideName = peptide,
            vialMg = savedStateHandle["calculator_vial_mg"],
            diluentMl = savedStateHandle["calculator_diluent_ml"],
            syringeUnitsPerMl = savedStateHandle["calculator_syringe"] ?: 100
        )
    }
}
