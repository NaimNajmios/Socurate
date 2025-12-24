package com.najmi.oreamnos.viewmodel

/**
 * Sealed class representing the UI state for content generation.
 * Used by MainViewModel to communicate state changes to UI.
 * 
 * Using sealed class instead of enum with builder for better Kotlin pattern matching.
 */
sealed class GenerationState {
    
    /** Initial state, no generation in progress */
    object Idle : GenerationState()
    
    /** Generation or refinement in progress */
    data class Loading(
        @get:JvmName("isRefinementValue")
        val isRefinement: Boolean = false
    ) : GenerationState()
    
    /** Generation completed successfully */
    data class Success(
        @get:JvmName("getTitleValue")
        val generatedTitle: String = "",
        @get:JvmName("getBodyValue")
        val generatedBody: String = "",
        @get:JvmName("getSourceValue")
        val sourceCitation: String = "",
        @get:JvmName("isRefinementValue")
        val isRefinement: Boolean = false
    ) : GenerationState() {
        /** Checks if there is generated content available. */
        override fun hasContent(): Boolean =
            generatedTitle.isNotEmpty() || generatedBody.isNotEmpty()
    }
    
    /** Generation failed */
    data class Error(
        @get:JvmName("getMessageValue")
        val errorMessage: String = "",
        @get:JvmName("isRefinementValue")
        val isRefinement: Boolean = false
    ) : GenerationState()
    
    /** Checks if there is generated content available for any state. */
    open fun hasContent(): Boolean = when (this) {
        is Success -> generatedTitle.isNotEmpty() || generatedBody.isNotEmpty()
        else -> false
    }

    // Java-compatible getter methods (these won't clash now due to @get:JvmName on properties)
    open fun getGeneratedTitle(): String = (this as? Success)?.generatedTitle ?: ""
    open fun getGeneratedBody(): String = (this as? Success)?.generatedBody ?: ""
    open fun getSourceCitation(): String = (this as? Success)?.sourceCitation ?: ""
    open fun getErrorMessage(): String = (this as? Error)?.errorMessage ?: ""
    open fun isRefinement(): Boolean = when (this) {
        is Loading -> isRefinement
        is Success -> isRefinement
        is Error -> isRefinement
        else -> false
    }
    
    companion object {
        /** Factory method for idle state - for Java interop */
        @JvmStatic
        fun idle(): GenerationState = Idle
        
        /** Factory method for loading state */
        @JvmStatic
        fun loading(isRefinement: Boolean): GenerationState = Loading(isRefinement)
        
        /** Factory method for success state */
        @JvmStatic
        fun success(
            title: String?,
            body: String?,
            source: String?,
            isRefinement: Boolean
        ): GenerationState = Success(
            generatedTitle = title ?: "",
            generatedBody = body ?: "",
            sourceCitation = source ?: "",
            isRefinement = isRefinement
        )
        
        /** Factory method for error state */
        @JvmStatic
        fun error(message: String?, isRefinement: Boolean): GenerationState =
            Error(errorMessage = message ?: "", isRefinement = isRefinement)
    }
}

// Extension properties for Kotlin access (optional, kept for Kotlin convenience)
val GenerationState.status: String
    get() = when (this) {
        is GenerationState.Idle -> "IDLE"
        is GenerationState.Loading -> "LOADING"
        is GenerationState.Success -> "SUCCESS"
        is GenerationState.Error -> "ERROR"
    }
