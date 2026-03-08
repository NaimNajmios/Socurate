package com.najmi.oreamnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel scoped to [com.najmi.oreamnos.MainActivity]'s ViewModelStoreOwner.
 *
 * Used to pass the article text from the Generate screen to the Card Generator screen.
 * This establishes an automatic sync bridge so the Card Generator can auto-fill
 * and extract data seamlessly as the user navigates.
 */
class AppViewModel : ViewModel() {

    private val _latestGeneratedText = MutableStateFlow("")
    val latestGeneratedText: StateFlow<String> = _latestGeneratedText.asStateFlow()

    private val _hasUnconsumedText = MutableStateFlow(false)
    val hasUnconsumedText: StateFlow<Boolean> = _hasUnconsumedText.asStateFlow()

    /**
     * Updates the latest generated output from the MainScreen.
     * Sets the unconsumed flag to true so the CardScreen knows to fetch it.
     */
    fun syncGeneratedText(text: String) {
        if (text.isNotBlank() && text != _latestGeneratedText.value) {
            _latestGeneratedText.value = text
            _hasUnconsumedText.value = true
        }
    }

    /**
     * Marks the synced text as consumed by the Card Generator so it doesn't
     * repeatedly trigger re-extractions on configuration changes.
     */
    fun markTextConsumed() {
        _hasUnconsumedText.value = false
    }
}
