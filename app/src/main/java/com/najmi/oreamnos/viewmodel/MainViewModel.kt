package com.najmi.oreamnos.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for MainActivity.
 * Holds UI state that survives configuration changes (like screen rotation).
 * Decouples MainActivity from business logic and state management.
 */
class MainViewModel : ViewModel() {

    // Current generation state
    private val _state = MutableLiveData<GenerationState>(GenerationState.Idle)
    val state: LiveData<GenerationState> = _state

    // Original input text for regeneration
    var originalInputText: String = ""
        private set

    // Store the original generated content (before user edits)
    var originalGeneratedPost: String = ""
        private set

    // Edit mode state
    var isEditMode: Boolean = false

    // Current input text (for restoration)
    var currentInputText: String = ""

    // User-edited content (if different from original)
    var userEditedContent: String = ""

    // Auto-generate flag triggered by Share intent
    var autoGenerateFlag: Boolean = false

    /**
     * Gets the current state value (non-observable).
     */
    fun getCurrentState(): GenerationState = _state.value ?: GenerationState.Idle

    /**
     * Sets the state to loading.
     */
    fun setLoading(isRefinement: Boolean) {
        _state.value = GenerationState.Loading(isRefinement)
    }

    /**
     * Sets the state to success with generated content.
     */
    fun setSuccess(title: String?, body: String?, source: String?, isRefinement: Boolean) {
        _state.value = GenerationState.Success(
            generatedTitle = title ?: "",
            generatedBody = body ?: "",
            sourceCitation = source ?: "",
            isRefinement = isRefinement
        )
    }

    /**
     * Sets the state to error.
     */
    fun setError(errorMessage: String?, isRefinement: Boolean) {
        _state.value = GenerationState.Error(
            errorMessage = errorMessage ?: "",
            isRefinement = isRefinement
        )
    }

    /**
     * Resets the state to idle.
     */
    fun resetState() {
        _state.value = GenerationState.Idle
        originalInputText = ""
        originalGeneratedPost = ""
        currentInputText = ""
        userEditedContent = ""
        isEditMode = false
    }

    /**
     * Sets the original input text.
     */
    fun setOriginalInputText(text: String?) {
        originalInputText = text ?: ""
    }

    /**
     * Sets the original generated post.
     */
    fun setOriginalGeneratedPost(post: String?) {
        originalGeneratedPost = post ?: ""
    }

    /**
     * Checks if there is content available.
     */
    fun hasContent(): Boolean = _state.value?.hasContent() ?: false

    companion object {
        private const val TAG = "MainViewModel"
    }
}
