package com.najmi.oreamnos.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared ViewModel scoped to [com.najmi.oreamnos.MainActivity]'s ViewModelStoreOwner.
 *
 * Used to pass the article text from the Generate screen to the Card Generator screen
 * when the user taps "Create Card" — avoiding Navigation argument size limits for long text.
 *
 * Both [com.najmi.oreamnos.cardgen.viewmodel.CardGeneratorViewModel] and
 * the main screen Composable read from [pipedArticleText].
 */
class AppViewModel : ViewModel() {

    private val _pipedArticleText = MutableStateFlow("")

    /**
     * The article text to pipe into the Card Generator.
     * Set by the Generate screen when the user taps "Create Card".
     * Consumed and cleared by the Card Generator screen.
     */
    val pipedArticleText: StateFlow<String> = _pipedArticleText.asStateFlow()

    /**
     * Sets the article text to pass to the Card Generator screen.
     */
    fun pipeText(text: String) {
        _pipedArticleText.value = text
    }

    /**
     * Clears the piped text after the Card Generator screen has consumed it.
     */
    fun clearPipedText() {
        _pipedArticleText.value = ""
    }
}
